package nl.kb.dare.model.reporting;

import nl.kb.dare.model.statuscodes.ErrorStatus;

import java.net.URL;
import java.time.Instant;

public class ErrorReport {

    private static final String PACKAGE_FILTER = "nl.kb";
    private final Exception exception;
    private final URL url;
    private final ErrorStatus errorStatus;
    private final String dateStamp;

    public Exception getException() {
        return exception;
    }

    public ErrorReport(Exception exception, URL url, ErrorStatus errorStatus) {
        this.exception = exception;
        this.url = url;
        this.errorStatus = errorStatus;
        this.dateStamp = Instant.now().toString();
    }

    public ErrorReport(Exception exception, ErrorStatus errorStatus) {
        this(exception, null, errorStatus);
    }

    public String getFilteredStackTrace() {
        final StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            if (element.getClassName().startsWith(PACKAGE_FILTER)) {
                sb.append(element.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    String getUrl() {
        return url == null ? "" : url.toString();
    }

    String getDateStamp() {
        return dateStamp;
    }

    String getErrorMessage() {
        return exception.getMessage();
    }


    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }
}
