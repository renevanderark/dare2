package nl.kb.dare.http.responsehandlers;

import nl.kb.dare.http.HttpResponseException;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class ErrorReportingResponseHandlerTest {

    private ErrorReportingResponseHandler instance;
    private static final URL THE_URL;
    static {
        try {
            THE_URL = new URL("http://example.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setup() throws MalformedURLException {
        instance = new ErrorReportingResponseHandler() {

            @Override
            public void onResponseData(Response.Status status, InputStream responseData) {

            }
        };

        instance.setUrl(THE_URL);
    }

    @Test
    public void onResponseErrorShouldAddAnHttpResponseErrorToTheListOfExceptions()  {
        instance.onResponseError(Response.Status.BAD_REQUEST, mock(InputStream.class));
        instance.onResponseError(Response.Status.INTERNAL_SERVER_ERROR, mock(InputStream.class));

        assertThat(instance.getExceptions(), contains(
            allOf(
                instanceOf(ErrorReport.class),
                hasProperty("url", is(THE_URL.toString())),
                hasProperty("errorStatus", is(ErrorStatus.BAD_REQUEST)),
                hasProperty("exception", is(instanceOf(HttpResponseException.class)))
            ), allOf(
                instanceOf(ErrorReport.class),
                hasProperty("url", is(THE_URL.toString())),
                hasProperty("errorStatus", is(ErrorStatus.INTERNAL_SERVER_ERROR)),
                hasProperty("exception", is(instanceOf(HttpResponseException.class)))
            )
        ));
    }

    @Test
    public void onRequestErrorShouldAddAnIoExceptionToTheListOfExceptions()  {
        instance.onRequestError(new IOException("problem"));
        instance.onRequestError(new SAXException("another problem"));

        assertThat(instance.getExceptions(), contains(
                allOf(
                    instanceOf(ErrorReport.class),
                    hasProperty("url", is(THE_URL.toString())),
                    hasProperty("errorStatus", is(ErrorStatus.IO_EXCEPTION)),
                    hasProperty("exception", is(instanceOf(IOException.class)))
                ), allOf(
                    instanceOf(ErrorReport.class),
                    hasProperty("url", is(THE_URL.toString())),
                    hasProperty("errorStatus", is(ErrorStatus.IO_EXCEPTION)),
                    hasProperty("exception", is(instanceOf(IOException.class)))
                )
        ));
    }

    @Test
    public void onRedirectShouldMaybeDoSomethingMaybeNot()  {
        instance.onRedirect("from", "to");
        // TODO ?
    }

    @Test(expected = Exception.class)
    public void throwAnyExceptionShouldThrowAnExceptionFromTheListOfExceptions() throws IOException, SAXException {
        instance.onRequestError(new IOException("problem"));
        instance.onResponseError(Response.Status.INTERNAL_SERVER_ERROR, mock(InputStream.class));

        instance.throwAnyException();
    }

    @Test
    public void getExceptionsShouldReturnTheListOfExceptions()  {
        instance.onResponseError(Response.Status.BAD_REQUEST, mock(InputStream.class));
        instance.onResponseError(Response.Status.INTERNAL_SERVER_ERROR, mock(InputStream.class));
        instance.onRequestError(new IOException("problem"));
        instance.onRequestError(new SAXException("another problem"));

        assertThat(instance.getExceptions(), containsInAnyOrder(
            allOf(
                    instanceOf(ErrorReport.class),
                    hasProperty("url", is(THE_URL.toString())),
                    hasProperty("errorStatus", is(ErrorStatus.BAD_REQUEST)),
                    hasProperty("exception", is(instanceOf(HttpResponseException.class)))
            ), allOf(
                    instanceOf(ErrorReport.class),
                    hasProperty("url", is(THE_URL.toString())),
                    hasProperty("errorStatus", is(ErrorStatus.INTERNAL_SERVER_ERROR)),
                    hasProperty("exception", is(instanceOf(HttpResponseException.class)))
            ), allOf(
                    instanceOf(ErrorReport.class),
                    hasProperty("url", is(THE_URL.toString())),
                    hasProperty("errorStatus", is(ErrorStatus.IO_EXCEPTION)),
                    hasProperty("exception", is(instanceOf(IOException.class)))
            ), allOf(
                    instanceOf(ErrorReport.class),
                    hasProperty("url", is(THE_URL.toString())),
                    hasProperty("errorStatus", is(ErrorStatus.IO_EXCEPTION)),
                    hasProperty("exception", is(instanceOf(IOException.class)))
            )
        ));
    }

}