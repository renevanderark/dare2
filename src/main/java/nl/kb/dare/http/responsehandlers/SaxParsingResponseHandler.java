package nl.kb.dare.http.responsehandlers;

import com.google.common.collect.Lists;
import nl.kb.dare.http.HttpResponseHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.ws.rs.core.Response;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

class SaxParsingResponseHandler implements HttpResponseHandler {
    private final SAXParser saxParser;
    private URL url;

    {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final List<SAXException> saxExceptions = Lists.newArrayList();
    private final List<IOException> ioExceptions = Lists.newArrayList();

    private final DefaultHandler xmlHandler;

    SaxParsingResponseHandler(DefaultHandler xmlHandler) {
        this.xmlHandler = xmlHandler;
    }

    @Override
    public void onResponseData(Response.Status status, InputStream responseData) {
        try {
            saxParser.parse(responseData, xmlHandler);
        } catch (SAXException e) {
            saxExceptions.add(e);
        } catch (IOException e) {
            ioExceptions.add(e);
        }
    }

    @Override
    public void onResponseError(Response.Status status, InputStream responseData) {
        final String message = String.format("Url responded with status %d - %s: %s",
                status.getStatusCode(), status.getReasonPhrase(), url);

        ioExceptions.add(new IOException(message));
    }

    @Override
    public void onRequestError(Exception exception) {

        ioExceptions.add(new IOException(exception));
    }

    @Override
    public void onRedirect(String sourceLocation, String targetLocation) {
        // not expected in standard OAI endpoint
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public void throwAnyException() throws IOException, SAXException {
        if (ioExceptions.size() > 0) {
            throw ioExceptions.get(0);
        }
        if (saxExceptions.size() > 0) {
            throw saxExceptions.get(0);
        }
    }

    @Override
    public List<Exception> getExceptions() {
        List<Exception> exceptions = Lists.newArrayList();
        exceptions.addAll(ioExceptions);
        exceptions.addAll(saxExceptions);
        return exceptions;
    }
}
