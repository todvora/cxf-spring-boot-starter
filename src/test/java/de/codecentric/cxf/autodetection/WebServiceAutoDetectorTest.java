package de.codecentric.cxf.autodetection;

import de.codecentric.cxf.TestServiceEndpoint;
import de.codecentric.cxf.common.BootStarterCxfException;
import de.codecentric.cxf.diagnostics.SeiImplClassNotFoundException;
import de.codecentric.cxf.diagnostics.SeiNotFoundException;
import de.codecentric.cxf.diagnostics.WebServiceClientNotFoundException;
import de.codecentric.namespace.weatherservice.Weather;
import de.codecentric.namespace.weatherservice.WeatherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;

import java.util.Arrays;
import java.util.List;

import static de.codecentric.cxf.autodetection.WebServiceAutoDetector.SEI_ANNOTATION;
import static de.codecentric.cxf.autodetection.WebServiceAutoDetector.WEB_SERVICE_CLIENT_ANNOTATION;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class WebServiceAutoDetectorTest {

    private static final Class WEATHER_SERVICE_ENDPOINT_INTERFACE = WeatherService.class;
    private static final Class WEATHER_WEBSERVICE_CLIENT = Weather.class;
    private static final Class WEATHER_SEI_IMPLEMENTING_CLASS = TestServiceEndpoint.class;

    private final BootStarterCxfException STARTER_EXCEPTION_NO_CLASS_FOUND = new BootStarterCxfException(WebServiceScanner.NO_CLASS_FOUND);

    @Test public void
    is_SEI_Successfully_detected() throws BootStarterCxfException {

        WebServiceScanner scannerMock = mock(WebServiceScanner.class);
        when(scannerMock.scanForClassWithAnnotationAndIsAnInterface(SEI_ANNOTATION)).thenReturn(WEATHER_SERVICE_ENDPOINT_INTERFACE);
        WebServiceAutoDetector webServiceAutoDetector = new WebServiceAutoDetector(scannerMock);

        Class serviceEndpointInterface = webServiceAutoDetector.searchServiceEndpointInterface();

        assertThat(serviceEndpointInterface, is(notNullValue()));
        assertThat(serviceEndpointInterface.getSimpleName(), is(equalTo(WEATHER_SERVICE_ENDPOINT_INTERFACE.getSimpleName())));
    }

    @Test public void
    is_SEI_Implementation_successfully_found_and_instantiated() throws NoSuchFieldException, BootStarterCxfException {

        WebServiceScanner scannerMock = mock(WebServiceScanner.class);
        when(scannerMock.scanForClassWhichImplementsAndPickFirst(WEATHER_SERVICE_ENDPOINT_INTERFACE)).thenReturn(WEATHER_SEI_IMPLEMENTING_CLASS);
        WebServiceAutoDetector autoDetector = new WebServiceAutoDetector(scannerMock);

        WeatherService weatherServiceEndpointImpl = autoDetector.searchAndInstantiateSeiImplementation(WEATHER_SERVICE_ENDPOINT_INTERFACE);

        assertThat(weatherServiceEndpointImpl, is(notNullValue()));
        assertThat(weatherServiceEndpointImpl.getClass().getSimpleName(), is(equalTo(WEATHER_SEI_IMPLEMENTING_CLASS.getSimpleName())));
    }

    @Test public void
    is_WebServiceClient_successfully_found_and_instantiated() throws BootStarterCxfException {

        WebServiceScanner scannerMock = mock(WebServiceScanner.class);
        when(scannerMock.scanForClassWithAnnotationAndPickTheFirstOneFound(WEB_SERVICE_CLIENT_ANNOTATION)).thenReturn(WEATHER_WEBSERVICE_CLIENT);
        WebServiceAutoDetector autoDetector = new WebServiceAutoDetector(scannerMock);

        Service webServiceClient = autoDetector.searchAndInstantiateWebServiceClient();

        assertThat(webServiceClient, is(notNullValue()));

        QName serviceNameQName = webServiceClient.getServiceName();
        assertThat(serviceNameQName.getLocalPart(), is(equalTo(WEATHER_WEBSERVICE_CLIENT.getSimpleName())));
    }

    @Test(expected = SeiImplClassNotFoundException.class) public void
    should_react_with_custom_startup_Failure_Message_console_if_SEI_implementing_class_is_missing() throws BootStarterCxfException {

        WebServiceScanner scannerMock = mock(WebServiceScanner.class);
        when(scannerMock.scanForClassWhichImplementsAndPickFirst(WEATHER_SERVICE_ENDPOINT_INTERFACE)).thenThrow(STARTER_EXCEPTION_NO_CLASS_FOUND);
        WebServiceAutoDetector autoDetector = new WebServiceAutoDetector(scannerMock);

        autoDetector.searchAndInstantiateSeiImplementation(WEATHER_SERVICE_ENDPOINT_INTERFACE);
    }

    @Test(expected = WebServiceClientNotFoundException.class) public void
    should_react_with_custom_startup_Failure_Message_if_WebServiceClient_annotated_class_is_missing() throws BootStarterCxfException {

        WebServiceScanner scannerMock = mock(WebServiceScanner.class);
        when(scannerMock.scanForClassWithAnnotationAndPickTheFirstOneFound(WEB_SERVICE_CLIENT_ANNOTATION)).thenThrow(STARTER_EXCEPTION_NO_CLASS_FOUND);
        WebServiceAutoDetector autoDetector = new WebServiceAutoDetector(scannerMock);

        autoDetector.searchAndInstantiateWebServiceClient();
    }

    @Test(expected = SeiNotFoundException.class) public void
    should_react_with_custom_startup_Failure_Message_if_SEI_is_missing() throws BootStarterCxfException {

        WebServiceScanner scannerMock = mock(WebServiceScanner.class);
        when(scannerMock.scanForClassNamesWithAnnotation(SEI_ANNOTATION)).thenThrow(STARTER_EXCEPTION_NO_CLASS_FOUND);
        when(scannerMock.scanForClassWithAnnotationAndIsAnInterface(SEI_ANNOTATION)).thenCallRealMethod();
        WebServiceAutoDetector autoDetector = new WebServiceAutoDetector(scannerMock);

        autoDetector.searchServiceEndpointInterface();
    }

    protected static List<String> generateListWithSeiAndSeiImplNameWithBothWebServiceAnnotation() {
        return Arrays.asList(WEATHER_SERVICE_ENDPOINT_INTERFACE.getName(), WEATHER_SEI_IMPLEMENTING_CLASS.getName());
    }

}
