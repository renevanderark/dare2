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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OaiRecord oaiRecord = (OaiRecord) o;

        if (identifier != null ? !identifier.equals(oaiRecord.identifier) : oaiRecord.identifier != null) return false;
        if (dateStamp != null ? !dateStamp.equals(oaiRecord.dateStamp) : oaiRecord.dateStamp != null) return false;
        if (status != null ? !status.equals(oaiRecord.status) : oaiRecord.status != null) return false;
        return repositoryId != null ? repositoryId.equals(oaiRecord.repositoryId) : oaiRecord.repositoryId == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (dateStamp != null ? dateStamp.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (repositoryId != null ? repositoryId.hashCode() : 0);
        return result;
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
