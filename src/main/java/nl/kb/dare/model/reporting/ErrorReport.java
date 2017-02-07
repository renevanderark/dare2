package nl.kb.dare.model.reporting;

import java.net.URL;
import java.time.Instant;

public class ErrorReport {

    private static final String PACKAGE_FILTER = "nl.kb";
    private final Exception exception;
    private final URL url;
    private final String dateStamp;

    public Exception getException() {
        return exception;
    }

    public ErrorReport(Exception exception, URL url) {
        this.exception = exception;
        this.url = url;
        this.dateStamp = Instant.now().toString();
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
}
