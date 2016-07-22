/*
 * MBeanInvocationHandler.java
 *                ,
 * Copyright 2006 Eamonn McManus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smeo.experiments.utils.jmx;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MBeanInvocationHandler implements InvocationHandler {
	private final WeakReference<Object> wrappedReference;

	public MBeanInvocationHandler(Object wrapped) {
		this.wrappedReference = new WeakReference<Object>(wrapped);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Class<?> wrappedClass = wrappedReference.get().getClass();
		Method methodInWrapped = wrappedClass.getMethod(method.getName(),
				method.getParameterTypes());
		try {
			return methodInWrapped.invoke(wrappedReference.get(), args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
