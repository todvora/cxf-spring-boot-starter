package de.codecentric.cxf.autodetection;

import de.codecentric.cxf.common.BootStarterCxfException;
import de.codecentric.cxf.diagnostics.SeiImplClassNotFoundException;
import de.codecentric.cxf.diagnostics.SeiNotFoundException;
import de.codecentric.cxf.diagnostics.WebServiceClientNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class WebServiceAutoDetector {

    private static final Logger LOG = LoggerFactory.getLogger(WebServiceAutoDetector.class);
    protected static final String NO_CLASS_FOUND = "No class found";
    private final WebServiceScanner webServiceScanner;

    public static final Class<WebService> SEI_ANNOTATION = WebService.class;
    public static final Class<WebServiceClient> WEB_SERVICE_CLIENT_ANNOTATION = WebServiceClient.class;

    public WebServiceAutoDetector(WebServiceScanner webServiceScanner) {
        this.webServiceScanner = webServiceScanner;
    }

    @SuppressWarnings("unchecked")
    public <T> T searchAndInstantiateSeiImplementation(Class seiName) throws BootStarterCxfException {
        Class<T> implementingClass = null;
        try {
            implementingClass = webServiceScanner.scanForClassWhichImplementsAndPickFirst(seiName);
            LOG.info("Found SEI implementing class: '{}'", implementingClass.getName());
        } catch (BootStarterCxfException exception) {
            throw SeiImplClassNotFoundException.build().setNotFoundClassName(seiName.getName());
        }
        return instantiateFromClass(implementingClass);
    }

    public Class searchServiceEndpointInterface() throws BootStarterCxfException {
        try{
            Class sei = webServiceScanner.scanForClassWithAnnotationAndIsAnInterface(SEI_ANNOTATION);
            LOG.info("Found Service Endpoint Interface (SEI): '{}'", sei.getName());
            return sei;
        } catch (BootStarterCxfException exception) {
            throw new SeiNotFoundException();
        }
    }

    @SuppressWarnings("unchecked")
    public Service searchAndInstantiateWebServiceClient() throws BootStarterCxfException {
        try{
            Class<Service> webServiceClientClass = webServiceScanner.scanForClassWithAnnotationAndPickTheFirstOneFound(WEB_SERVICE_CLIENT_ANNOTATION);
            LOG.info("Found WebServiceClient class: '{}'", webServiceClientClass.getName());
            return instantiateFromClass(webServiceClientClass);
        } catch (BootStarterCxfException exception) {
            throw new WebServiceClientNotFoundException();
        }
    }

    private <T> T instantiateFromClass(Class<T> clazz) throws BootStarterCxfException {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();

        } catch (NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException exception) {
            throw new BootStarterCxfException("Class couldn´t be instantiated", exception);
        }
    }

    protected Class<?> classForName(String className) throws BootStarterCxfException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new BootStarterCxfException(NO_CLASS_FOUND, exception);
        }
    }

}
