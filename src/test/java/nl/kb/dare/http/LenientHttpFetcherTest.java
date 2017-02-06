package nl.kb.dare.http;

import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LenientHttpFetcherTest {



    private URL makeUrl(InputStream responseData, HttpURLConnection urlConnection) throws IOException {

        doReturn(responseData).when(urlConnection).getInputStream();

        final URLStreamHandler urlStreamHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return urlConnection;
            }
        };
        return new URL("http", "example.com", 80, "/", urlStreamHandler);
    }



    @Test
    public void executeShouldPassInputStreamAndResponseCodeToHandler() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);
        when(connection.getResponseCode()).thenReturn(200);

        instance.execute(url, responseHandler);

        verify(responseHandler).onResponseData(Response.Status.OK, responseData);
        verify(responseHandler, never()).onRequestError(any());
        verify(responseHandler, never()).onResponseError(any(), any());
        verify(responseHandler, never()).onRedirect(any(), any());
    }

    @Test
    public void executeShouldHandleRedirects() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);
        final String redirectLocation = "<< force fail with invalid url >>";
        when(connection.getResponseCode()).thenReturn(301);
        when(connection.getHeaderField("Location")).thenReturn(redirectLocation);

        instance.execute(url, responseHandler);

        verify(responseHandler).onRedirect("http://example.com:80/", redirectLocation);
        verify(responseHandler).onRequestError(any(Exception.class));
        verify(responseHandler, never()).onResponseError(any(), any());
        verify(responseHandler, never()).onResponseData(any(), any());
    }
}