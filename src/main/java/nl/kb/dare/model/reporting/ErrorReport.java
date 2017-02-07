package nl.kb.dare.model.reporting;

import java.net.URL;

public class ErrorReport {

    private static final String PACKAGE_FILTER = "nl.kb";
    private final Exception exception;
    private final URL url;

    public Exception getException() {
        return exception;
    }

    public ErrorReport(Exception exception, URL url) {
        this.exception = exception;
        this.url = url;
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
}
