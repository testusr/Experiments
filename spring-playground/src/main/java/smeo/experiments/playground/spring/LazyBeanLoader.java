package smeo.experiments.playground.spring;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by smeo on 06.08.16.
 */
public class LazyBeanLoader {
    private final GenericApplicationContext appContext;
    private final XmlBeanDefinitionReader reader;
    private final AtomicBoolean contextWasRefreshed = new AtomicBoolean(false);


    public LazyBeanLoader(){
        this(false);
    }

    public LazyBeanLoader(boolean forceBeanDefinitionReader) {
        appContext = new GenericApplicationContext();

        // me the registry lazy loading
        if (!forceBeanDefinitionReader) {
            reader = new DefaultLazyInitBeanDefinitionReader(appContext);
        } else {
            reader = new XmlBeanDefinitionReader(appContext);
        }
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        // add annotation post processors support in the given context. triggered on context.refresh() only
        AnnotationConfigUtils.registerAnnotationConfigProcessors(appContext);
    }

    public void loadBeanDefinitions(String filename){
        File file = new File(filename);
        if (file.exists()) {
            try {
                FileInputStream configInputStream = new FileInputStream(filename);
                System.out.println("File '"+configInputStream+"' exists and configuration loaded");
                loadBeanDefinitions(configInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // must be XML
    public void loadBeanDefinitions(InputStream is) {
        reader.loadBeanDefinitions(new InputStreamResource(is));
    }

    public Object createBean(String name) {
        refreshContextIfNecessary();
        return appContext.getBean(name);
    }

    public Object getBean(String name) {
        refreshContextIfNecessary();
        return appContext.getBean(name);
    }

    public void initAllNonLazyBeans() {
        this.appContext.getBeanFactory().preInstantiateSingletons();
    }

    /**
     * this is a really dirty hack to make the class spring 4 compliant
     * before any bean can be fetched the context has to be refreshed.
     * This is fully relying on that no bean is accessed before all bean definitions have been
     * loaded. After the context has been refreshed no beans can be loaded anymore.
     */
    private void refreshContextIfNecessary() {
        if (!contextWasRefreshed.getAndSet(true)) {
            try {
                appContext.refresh();
            } catch (RuntimeException e) {
                throw e;
            } finally {
                contextWasRefreshed.set(appContext.isActive());
            }
        }
    }

    private class DefaultLazyInitBeanDefinitionReader extends XmlBeanDefinitionReader {
        public DefaultLazyInitBeanDefinitionReader(GenericApplicationContext appContext) {
            super(appContext);
        }

        @Override
        protected Document doLoadDocument(InputSource inputSource, Resource resource) throws Exception {
            final Document document = super.doLoadDocument(inputSource, resource);
            return makeDocumentDefaultLazyInitIfNotSet(document);
        }

        private Document makeDocumentDefaultLazyInitIfNotSet(Document document) {
            final NodeList beans = document.getElementsByTagName("bean");
            for (int i = 0; i < beans.getLength(); i++) {
                final Node item = beans.item(i);
                if (item.getNodeName().trim().equalsIgnoreCase("bean")) {
                    ((Element) item).setAttribute("lazy-init", "true");
                }
            }
            return document;
        }
    }
}
