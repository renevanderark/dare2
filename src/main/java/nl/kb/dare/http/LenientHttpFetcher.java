package nl.kb.dare.http;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class LenientHttpFetcher implements HttpFetcher {

    private final boolean proactivelyClosing;

    public LenientHttpFetcher(boolean proactivelyClosing) {
        this.proactivelyClosing = proactivelyClosing;
    }

    public void execute(URL url, HttpResponseHandler responseHandler) {
        responseHandler.setUrl(url);
        final Optional<HttpURLConnection> connectionOpt = getConnection(url, responseHandler);
        if (!connectionOpt.isPresent()) { return; }

        final HttpURLConnection connection = connectionOpt.get();
        if (proactivelyClosing) {
            connection.setRequestProperty("Connection", "close");
        }

        final Optional<Integer> responseCode = getResponseCode(connection, responseHandler);
        if (!responseCode.isPresent()) { return; }


        final Integer statusCode = responseCode.get();
        final Optional<InputStream> responseDataOpt = getResponseData(connection, responseHandler);
        if (!responseDataOpt.isPresent()) { return; }


        final InputStream responseData = responseDataOpt.get();
        if (statusCode >= 200 && statusCode < 300 ) {
            responseHandler.onResponseData(Response.Status.fromStatusCode(statusCode), responseData);
        } else if (statusCode >= 300 && statusCode < 400) {
            final String redirectLocation = connection.getHeaderField("Location");
            responseHandler.onRedirect(url.toString(), redirectLocation);

            if (redirectLocation == null) {
                responseHandler.onResponseError(Response.Status.fromStatusCode(statusCode), responseData);
                return;
            }

            try {
                execute(new URL(redirectLocation), responseHandler);
            } catch (MalformedURLException e) {
                responseHandler.onRequestError(e);
            }

        } else {
            responseHandler.onResponseError(Response.Status.fromStatusCode(statusCode), responseData);
        }

    }

    private Optional<Integer> getResponseCode(HttpURLConnection connection, HttpResponseHandler responseHandler)  {
        try {
            return Optional.of(connection.getResponseCode());
        } catch (IOException e) {
            responseHandler.onRequestError(e);
            return Optional.empty();
        }
    }

    private Optional<InputStream> getResponseData(HttpURLConnection connection, HttpResponseHandler responseHandler) {
        try {
            final InputStream inputStream = connection.getInputStream();
            return  Optional.of(inputStream);
        } catch (IOException e) {
            responseHandler.onRequestError(e);
            return Optional.empty();
        }
    }

    private Optional<HttpURLConnection> getConnection(URL url, HttpResponseHandler responseHandler) {
        try {
            return Optional.of((HttpURLConnection) url.openConnection());
        } catch (IOException e) {
            responseHandler.onRequestError(e);
            return Optional.empty();
        }
    }
}
