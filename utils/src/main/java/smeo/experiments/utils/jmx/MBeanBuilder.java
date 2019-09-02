/*
 * MBeanBuilder.java
 */

package smeo.experiments.utils.jmx;

import org.apache.commons.lang3.Validate;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides several methods to register objects as mbeans. The necessary \<classname\>MBean interfaces
 * get dynamically created from the object methods tagged with the @Managed annotation.
 * The registrations do not prevent objects from cleaned up. If the managed objects get garbage collected
 * the corresponding MBeans get automatically deregistered.
 * 
 */
public class MBeanBuilder {
	protected final static Set<BeanRegistration> beanRegistrations = new CopyOnWriteArraySet<BeanRegistration>();
	private static final MBeanBuilder builder = new MBeanBuilder();
	private final static Timer cleanupTimer = new Timer("MBeanBuilder-CleanupTimer", true);
	private final static TimerTask cleanupTimerTask;

	private final static AtomicBoolean isCleanupRunning = new AtomicBoolean(false);
	// protected static final Logger LOGGER = Log.getLogger(MBeanBuilder.class);

	private final InterfaceClassLoader loader;

	static {
		cleanupTimerTask = new TimerTask() {

			@Override
			public void run() {
				unregisterMBeansForGarbageCollectedObjects();
			}

		};
		cleanupTimer.schedule(cleanupTimerTask, 10000, 10000);
	}

	public MBeanBuilder() {
		loader = new InterfaceClassLoader(getClass().getClassLoader());
	}

	public static ObjectName createObjectName(String mBeanName, String beanType) {
		String fullObjectName = null;
		try {
			String cleanedMBeanName = replaceNotAllowedCharacters(mBeanName);
			String cleanedMBeanType = replaceNotAllowedCharacters(beanType);

			fullObjectName = cleanedMBeanName + ":type=" + cleanedMBeanType;
			return new ObjectName(fullObjectName);
		} catch (Exception e) {
			// LOGGER.warn(LogEvent.create(".createObjectName", "caught exception, rethrowing as runtime exception", e)
			// .add("mBeanName", mBeanName)
			// .add("beanType", beanType)
			// .add("fullObjectName", fullObjectName));
			throw new IllegalArgumentException(e);
		}
	}

	public static ObjectName createObjectNameWithSubfolderHierarchy(String mBeanName, String domain, String... subfolderHierachy)
			throws MalformedObjectNameException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(domain);
		stringBuilder.append(":");
		for (int i = 0; i < subfolderHierachy.length; i++) {
			stringBuilder.append(String.format("%02d", i));
			stringBuilder.append("=");
			stringBuilder.append(replaceNotAllowedCharacters(subfolderHierachy[i]));
			stringBuilder.append(",");
		}
		stringBuilder.append("name=");
		stringBuilder.append(mBeanName);

		ObjectName objectName = new ObjectName(stringBuilder.toString());
		return objectName;
	}

	public static void generateAndRegisterMBean(Object managedObject, ObjectName objectName, boolean overwriteExisting)
			throws MBeanRegistrationException, NotCompliantMBeanException, NullPointerException {

		if (!isManageableClass(managedObject.getClass())) {
			// LOGGER.info(LogEvent.create(".generateAndRegisterMBean", "not manageable class")
			// .add("class", managedObject.getClass().getName()));
			return;
		}
		Object mbean = builder.buildMBean(managedObject);
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		// LOGGER.info(LogEvent.create(".generateAndRegisterMBean", "")
		// .addObject("objectName", objectName));
		if (overwriteExisting) {
			BeanRegistration beanRegistration = getRegisteredMBean(objectName);
			if (beanRegistration != null) {
				// LOGGER.warn(LogEvent.create(".generateAndRegisterMBean", "unregistering mbean to replace it with new one")
				// .addObject("objectName", objectName));
				beanRegistration.unregisterMbean();
			}
		}

		try {
			mBeanServer.registerMBean(mbean, objectName);
		} catch (InstanceAlreadyExistsException e) {
			// LOGGER.warn(LogEvent.create(".generateAndRegisterMBean", "bean already exists, failed to add another")
			// .addObject("objectName", objectName));
		}
		BeanRegistration mbeanRegistration = new BeanRegistration(mBeanServer, objectName, managedObject);
		beanRegistrations.add(mbeanRegistration);
		mbeanRegistration.markCompleted();

	}

	/**
	 * Usage:
	 * <p>
	 * { try { MBeanBuilder.generateAndRegisterMBean(this, this.getClass().getName() + "-" + getCompany().getName()); } catch (Exception e) {
	 * //LOGGER.error(LogEvent.create(".CompanyMarketDataPool", "caught exception while trying to generate and register mBean", e)); }
	 * </p>
	 * 
	 * @param managedObject
	 * @param mBeanName
	 *            the bean name to be used, without any ":type=" parameters etc
	 */
	public static ObjectName generateAndRegisterMBean(Object managedObject, String mBeanName)
			throws MBeanRegistrationException, NotCompliantMBeanException, NullPointerException {
		ObjectName objectName = createObjectName(mBeanName, managedObject.getClass().getSimpleName());
		generateAndRegisterMBean(managedObject, objectName, true);
		return objectName;
	}

	/**
	 * Registers {@code managedObject} as a JMX mean.
	 * If operation is successful then returns the {@link ObjectName } of the JMX bean that was registered.
	 * If an error occurs during registration it will log an error and return null.
	 * 
	 * @param managedObject
	 *            an object to be registered as a JMX bean
	 * @param mBeanName
	 *            JMX bean name
	 * @return {@link ObjectName } of the JMX bean that was suscessfully registered, null otherwise
	 */
	public static ObjectName generateAndRegisterMBeanQuietly(Object managedObject, String mBeanName) {
		try {
			return generateAndRegisterMBean(managedObject, mBeanName);
		} catch (Exception e) {
			// LOGGER.error(LogEvent.create("generateAndRegisterMBeanQuietly", "cannot register mbean", e));
			return null;
		}
	}

	public static ObjectName generateAndRegisterMBean(Object managedObject, String mBeanName, String typeAddOn)
			throws MBeanRegistrationException, NotCompliantMBeanException, NullPointerException {
		ObjectName objectName = createObjectName(mBeanName, managedObject.getClass().getSimpleName() + "-" + typeAddOn);
		generateAndRegisterMBean(managedObject, objectName, true);
		return objectName;
	}

	/**
	 * Puts the managedObjed into a JMX folder structure: rootFolderName/type/object and shows it with the given name.
	 * 
	 * @param managedObject
	 * @param rootFolderName
	 * @param type
	 * @param object
	 *            (set to null if you don't need it)
	 * @param name
	 *            (if null the managedObject's simple name is used)
	 * @throws MalformedObjectNameException
	 * @throws NullPointerException
	 * @throws MBeanRegistrationException
	 * @throws NotCompliantMBeanException
	 */
	public static void generateAndRegisterMBean(Object managedObject, String rootFolderName, String type, String object, String name)
			throws MalformedObjectNameException, NullPointerException, MBeanRegistrationException, NotCompliantMBeanException {
		Validate.notNull(managedObject, "managedObject must not be null");
		Validate.notNull(rootFolderName, "rootFolderName must not be null");
		Validate.notNull(type, "type must not be null");
		Validate.notNull(rootFolderName, "rootFolderName must not be null");

		final StringBuilder nameBuilder = new StringBuilder(rootFolderName + ":type=" + replaceNotAllowedCharacters(type));
		if (object != null) {
			nameBuilder.append(",object=" + replaceNotAllowedCharacters(object));
		}
		nameBuilder.append(",name=" + (name != null ? replaceNotAllowedCharacters(name) : managedObject.getClass().getSimpleName()));

		generateAndRegisterMBean(managedObject, new ObjectName(nameBuilder.toString()), true);
	}

	/**
	 * Enabling to create a hierarchical mbean structure containing sub-folders which can themselves can contain mbeans.
	 * 
	 * @param managedObject
	 * @param mBeanName
	 *            the final name of the mbean within the described folder
	 * @param domain
	 *            the base domain where the subfolders should be placed in
	 * @param overwriteExisting
	 * @param subfolderHierachy
	 *            the folder structure example ["mainFolder","subfolder_l1","subfolder_l2"]
	 * @throws MBeanRegistrationException
	 * @throws NotCompliantMBeanException
	 * @throws NullPointerException
	 * @throws MalformedObjectNameException
	 */
	public static void generateAndRegisterMBeanInSubFolder(Object managedObject, String mBeanName, String domain, boolean overwriteExisting,
			String... subfolderHierachy)
			throws MBeanRegistrationException, NotCompliantMBeanException, NullPointerException, MalformedObjectNameException {
		ObjectName objectName = createObjectNameWithSubfolderHierarchy(mBeanName, domain, subfolderHierachy);
		generateAndRegisterMBean(managedObject, objectName, overwriteExisting);
	}

	public static boolean isManageableClass(Class<?> manageable) {

		for (Method method : manageable.getMethods()) {
			if (method.isAnnotationPresent(Managed.class)) {
				return true;
			}
		}

		return false;
	}

	public static void stopCleanUpTime() {
		try {
			cleanupTimer.cancel();
		} catch (Exception e) {
			// LOGGER.warn(LogEvent.create(".stopCleanUpTimeTask", "caught exceptio while trying to cancel timer", e));
		}
	}

	public static void unregisterMBean(Object managedObject) {
		for (BeanRegistration currBeanRegistration : beanRegistrations) {
			if (currBeanRegistration.getManagedObject() == managedObject) {
				try {
					currBeanRegistration.unregisterMbean();
					return;
				} catch (Exception e) {
					// LOGGER.warn(LogEvent.create(".unregisterMBean", "caught exception while trying to unregister MBean", e));
				}
			}
		}
	}

	/**
	 * Mark an MBean as perm
	 * 
	 * @param managedObject
	 */
	public static void makeBeanPermanent(Object managedObject) {
		for (BeanRegistration currBeanRegistration : beanRegistrations) {
			if (currBeanRegistration.getManagedObject() == managedObject) {
				try {
					currBeanRegistration.makePermanent();
					return;
				} catch (Exception e) {
					// LOGGER.warn(LogEvent.create(".unregisterMBean", "caught exception while trying to unregister MBean", e));
				}
			}
		}
	}

	public static void unregisterMBean(ObjectName objectName) {
		BeanRegistration beanRegistration = getRegisteredMBean(objectName);
		if (beanRegistration != null) {
			beanRegistration.unregisterMbean();
			return;
		}
		// LOGGER.warn(LogEvent.create(".unregisterMBean", "could not find a registered bean with name")
		// .addObject("objectName", objectName));
	}

	public static void unregisterMBeansForGarbageCollectedObjects() {
		if (!isCleanupRunning.getAndSet(true)) {
			try {
				Set<BeanRegistration> registrationsToBeRemoved = new HashSet<BeanRegistration>();
				for (BeanRegistration currBeanRegistration : beanRegistrations) {
					if (!currBeanRegistration.isPermanentBean() && currBeanRegistration.unregisterMbeanIfManagedObjectIsGarbageCollected()) {
						registrationsToBeRemoved.add(currBeanRegistration);
					}
				}
				if (registrationsToBeRemoved.size() > 0) {
					// LOGGER.info(LogEvent.create(".unregisterMBeansForGarbageCollectedObjects",
					// "automatically cleaned up mBeans, as managed objects got garbage collected")
					// .addObject("mBeansToUnregister", registrationsToBeRemoved));
				}

			} catch (Exception e) {
				// LOGGER.error(LogEvent.create(".unregisterMBeansForGarbageCollectedObjects", "caught exception while trying to clean up", e));
			}
			isCleanupRunning.set(false);
		}
	}

	private static BeanRegistration getRegisteredMBean(ObjectName objectName) {
		for (BeanRegistration currBeanRegistration : beanRegistrations) {
			if (currBeanRegistration.getObjectName().equals(objectName)) {
				try {
					return currBeanRegistration;
				} catch (Exception e) {
					// LOGGER.warn(LogEvent.create(".unregisterMBean", "caught exception while trying to unregister MBean", e));
				}
			}
		}
		return null;
	}

	private static <T> StandardMBean makeStandardMBean(Class<T> intf, InvocationHandler handler) {
		Object proxy = Proxy.newProxyInstance(intf.getClassLoader(),
				new Class<?>[] { intf },
				handler);
		T impl = intf.cast(proxy);
		try {
			return new StandardMBean(impl, intf);
		} catch (NotCompliantMBeanException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static String replaceNotAllowedCharacters(String stringValueToClean) {
		boolean charactersChanged = false;
		String cleanStringValue = stringValueToClean;
		if (cleanStringValue.contains("=")) {
			cleanStringValue = cleanStringValue.replace('=', '_');
			charactersChanged = true;
		}
		if (cleanStringValue.contains(":")) {
			cleanStringValue = cleanStringValue.replace(':', '_');
			charactersChanged = true;
		}
		if (cleanStringValue.contains(",")) {
			cleanStringValue = cleanStringValue.replace(',', '_');
			charactersChanged = true;
		}

		return cleanStringValue;
	}

	public StandardMBean buildMBean(Object x) {
		Class<?> c = x.getClass();
		Class<?> mbeanInterface = makeInterface(c);
		InvocationHandler handler = new MBeanInvocationHandler(x);
		return makeStandardMBean(mbeanInterface, handler);
	}

	private Class<?> makeInterface(Class<?> implClass) {
		String interfaceName = implClass.getName() + "$WrapperMBean";
		try {
			return Class.forName(interfaceName, false, loader);
		} catch (ClassNotFoundException e) {
			// OK, we'll build it
		}
		Set<XMethod> methodSet = new LinkedHashSet<XMethod>();
		for (Method m : implClass.getMethods()) {
			if (m.isAnnotationPresent(Managed.class)) {
				methodSet.add(new XMethod(m));
			}
		}
		for (Method m : implClass.getDeclaredMethods()) {
			if (m.isAnnotationPresent(Managed.class)) {
				methodSet.add(new XMethod(m));
			}
		}

		if (methodSet.isEmpty()) {
			throw new IllegalArgumentException("Class has no @Managed methods: "
					+ implClass);
		}
		XMethod[] methods = methodSet.toArray(new XMethod[0]);
		return loader.findOrBuildInterface(interfaceName, methods);
	}

	protected static class BeanRegistration {
		private final WeakReference<Object> managedObjectReference;
		private final MBeanServer mBeanServer;
		private final ObjectName objectName;
		private final long creationTs;
		private volatile boolean isPermanentBean;
		private volatile int creationTime = -1;
		private volatile int liveTime = -1;

		public BeanRegistration(MBeanServer mBeanServer, ObjectName objectName, Object managedObject) {
			this.mBeanServer = mBeanServer;
			this.objectName = objectName;
			this.managedObjectReference = new WeakReference<Object>(managedObject);
			this.creationTs = System.currentTimeMillis();
			this.isPermanentBean = isPermanentBean;
		}

		public void makePermanent() {
			this.isPermanentBean = true;
		}

		/**
		 * mark mbean creation process as complete
		 */
		public void markCompleted() {
			this.creationTime = (int) (System.currentTimeMillis() - creationTs);
		}

		/**
		 * The time it took to create the mBean / the impact on the creating thread
		 */
		public int getCreationTime() {
			return creationTime;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			BeanRegistration other = (BeanRegistration) obj;
			if (mBeanServer == null) {
				if (other.mBeanServer != null) {
					return false;
				}
			} else if (!mBeanServer.equals(other.mBeanServer)) {
				return false;
			}
			if (objectName == null) {
				if (other.objectName != null) {
					return false;
				}
			} else if (!objectName.equals(other.objectName)) {
				return false;
			}
			return true;
		}

		/**
		 * @return the time of the mbean registration to its unregistration. If the bean is not unregistered
		 *         the time from its regsitration till now.
		 */
		public int getLiveTime() {
			if (liveTime == -1) {
				return (int) (System.currentTimeMillis() - this.creationTs);
			}
			return liveTime;
		}

		public Object getManagedObject() {
			return managedObjectReference.get();
		}

		public ObjectName getObjectName() {
			return objectName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mBeanServer == null) ? 0 : mBeanServer.hashCode());
			result = prime * result + ((objectName == null) ? 0 : objectName.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return objectName.toString();
		}

		public void unregisterMbean() {
			this.liveTime = getLiveTime();
			try {
				mBeanServer.unregisterMBean(objectName);
			} catch (MBeanRegistrationException e) {
				// LOGGER.error(LogEvent.create(".unregisterMbean", "catch exception while trying to unregister MBean", e));
			} catch (InstanceNotFoundException e) {
				// LOGGER.error(LogEvent.create(".unregisterMbean", "catch exception while trying to unregister MBean", e));
			}
			beanRegistrations.remove(this);
		}

		public boolean unregisterMbeanIfManagedObjectIsGarbageCollected() {
			if (managedObjectReference.get() == null) {
				return true;
			}
			return false;
		}

		/**
		 * @return true if this bean should not be cleaned up
		 */
		public boolean isPermanentBean() {
			return isPermanentBean;
		}
	}

}
