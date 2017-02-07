package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OaiRecord {
    private String identifier;
    private String dateStamp;
    private Status status;
    private Integer repositoryId;

    public enum Status {
        DELETED,
        PENDING
    }

    ;

    public OaiRecord() {

    }

    public OaiRecord(String identifier, String dateStamp, Status status, Integer repositoryId) {
        this.identifier = identifier;
        this.dateStamp = dateStamp;
        this.status = status;
        this.repositoryId = repositoryId;
    }

    @JsonProperty
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonProperty
    public String getDateStamp() {
        return dateStamp;
    }

    @JsonProperty
    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    @JsonProperty
    public Status getStatus() {
        return status;
    }

    @JsonProperty
    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonProperty
    public Integer getRepositoryId() {
        return repositoryId;
    }

    @JsonProperty
    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }

    @Override
    public String toString() {
        return "\nOaiRecord{" +
                "identifier='" + identifier + '\'' +
                ", dateStamp='" + dateStamp + '\'' +
                ", status=" + status +
                ", repositoryId=" + repositoryId +
                '}';
    }
}
