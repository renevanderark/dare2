package nl.kb.dare.http;

import nl.kb.dare.model.reporting.ErrorReport;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface HttpResponseHandler {

    void onResponseData(Response.Status status, InputStream responseData, Map<String, List<String>> headerFields);

    void onResponseError(Response.Status status, InputStream responseData);

    void onRequestError(Exception exception);

    void onRedirect(String sourceLocation, String targetLocation);

    void setUrl(URL url);

    void throwAnyException() throws IOException, SAXException;

    List<ErrorReport> getExceptions();
}
