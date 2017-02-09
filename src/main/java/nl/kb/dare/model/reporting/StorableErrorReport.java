package nl.kb.dare.model.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.statuscodes.ErrorStatus;

public class StorableErrorReport {
    private ErrorStatus errorStatus;
    private String url;
    private String dateStamp;
    private String filteredStackTrace;
    private String message;

    StorableErrorReport(ErrorReport errorReport) {
        this.message = errorReport.getErrorMessage();
        this.filteredStackTrace = errorReport.getFilteredStackTrace();
        this.dateStamp = errorReport.getDateStamp();
        this.url = errorReport.getUrl();
        this.errorStatus = errorReport.getErrorStatus();
    }

    StorableErrorReport(String message, String filteredStackTrace, String dateStamp, String url, ErrorStatus errorStatus) {
        this.message = message;
        this.filteredStackTrace = filteredStackTrace;
        this.dateStamp = dateStamp;
        this.url = url;
        this.errorStatus = errorStatus;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public String getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    @JsonProperty
    public String getFilteredStackTrace() {
        return filteredStackTrace;
    }

    public void setFilteredStackTrace(String filteredStackTrace) {
        this.filteredStackTrace = filteredStackTrace;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    public Integer getErrorStatusCode() {
        return errorStatus.getCode();
    }
}
