package nl.kb.dare.model.oai;

public class OaiRecord {
    private String identifier;
    private String dateStamp;
    private String status;
    private Integer repositoryId;


    public OaiRecord() {

    }

    public OaiRecord(String identifier, String dateStamp, String status, Integer repositoryId) {
        this.identifier = identifier;
        this.dateStamp = dateStamp;
        this.status = status;
        this.repositoryId = repositoryId;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public String getStatus() {
        return status;
    }

    public Integer getRepositoryId() {
        return repositoryId;
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
