package nl.kb.dare.http.responsehandlers;

import com.google.common.collect.Lists;
import nl.kb.dare.http.HttpResponseException;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public abstract class ErrorReportingResponseHandler implements HttpResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorReportingResponseHandler.class);

    final List<SAXException> saxExceptions = Lists.newArrayList();
    final List<IOException> ioExceptions = Lists.newArrayList();
    private final List<HttpResponseException> httpResponseExceptions = Lists.newArrayList();
    private URL url;

    @Override
    public void onResponseError(Response.Status status, InputStream responseData) {
        final String message = String.format("Url responded with status %d - %s",
                status.getStatusCode(), status.getReasonPhrase());

        httpResponseExceptions.add(new HttpResponseException(message, ErrorStatus.forCode(status.getStatusCode())));
    }

    @Override
    public void onRequestError(Exception exception) {
        ioExceptions.add(new IOException("Request error", exception));
    }

    @Override
    public void onRedirect(String sourceLocation, String targetLocation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Redirect detected from {} to {}", sourceLocation, targetLocation);
        }
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
    public List<ErrorReport> getExceptions() {
        final Stream<ErrorReport> errorReportStream = Stream.concat(
                ioExceptions.stream().map(ex -> new ErrorReport(ex, url, ErrorStatus.IO_EXCEPTION)),
                saxExceptions.stream().map(ex -> new ErrorReport(ex, url, ErrorStatus.XML_PARSING_ERROR))
        );

        return Stream.concat(errorReportStream,
                httpResponseExceptions.stream().map(ex -> new ErrorReport(ex, url, ex.getErrorStatus()))
        ).collect(toList());
    }
}
