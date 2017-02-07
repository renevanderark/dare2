package nl.kb.dare.model.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HarvesterErrorReport {

    private Long id;
    private Integer repositoryId;
    private String url;
    private String dateStamp;
    private String filteredStackTrace;
    private String message;

    // For runtime generation
    public HarvesterErrorReport(ErrorReport errorReport, Integer repositoryId) {
        this.message = errorReport.getErrorMessage();
        this.filteredStackTrace = errorReport.getFilteredStackTrace();
        this.dateStamp = errorReport.getDateStamp();
        this.url = errorReport.getUrl();
        this.repositoryId = repositoryId;
    }

    // For database retrieval
    public HarvesterErrorReport(String message, String filteredStackTrace, String dateStamp, String url, Long id, Integer repositoryId) {
        this.message = message;
        this.filteredStackTrace = filteredStackTrace;
        this.dateStamp = dateStamp;
        this.url = url;
        this.id = id;
        this.repositoryId = repositoryId;
    }

    @JsonProperty
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @JsonProperty
    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }
}
