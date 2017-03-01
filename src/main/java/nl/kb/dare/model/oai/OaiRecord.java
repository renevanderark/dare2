package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OaiRecord {
    private String identifier;
    private String dateStamp;
    private OaiStatus oaiStatus;
    private Integer repositoryId;
    private ProcessStatus processStatus;
    private Integer updateCount = 0;

    public OaiRecord() {

    }
    public OaiRecord(String identifier, String dateStamp, OaiStatus oaiStatus, Integer repositoryId,
                     ProcessStatus processStatus) {
        this.identifier = identifier;
        this.dateStamp = dateStamp;
        this.oaiStatus = oaiStatus;
        this.repositoryId = repositoryId;
        this.processStatus = processStatus;
    }

    public OaiRecord(String identifier, String dateStamp, OaiStatus oaiStatus, Integer repositoryId,
                     ProcessStatus processStatus, Integer updateCount) {
        this(identifier, dateStamp, oaiStatus, repositoryId, processStatus);
        this.updateCount = updateCount;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    public void setOaiStatus(OaiStatus oaiStatus) {
        this.oaiStatus = oaiStatus;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void setProcessStatus(ProcessStatus processStatus) {
        this.processStatus = processStatus;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public OaiStatus getOaiStatus() {
        return oaiStatus;
    }

    public Integer getRepositoryId() {
        return repositoryId;
    }

    public ProcessStatus getProcessStatus() {
        return processStatus;
    }

    public Integer getOaiStatusCode() { return oaiStatus.getCode(); }

    public Integer getProcessStatusCode() { return processStatus.getCode(); }

    @JsonProperty
    public void setOaiStatus(String oaiStatus) {
        this.oaiStatus = OaiStatus.forString(oaiStatus);
    }

    @JsonProperty
    public void setProcessStatus(String processStatus) {
        this.processStatus = ProcessStatus.forString(processStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OaiRecord oaiRecord = (OaiRecord) o;

        if (identifier != null ? !identifier.equals(oaiRecord.identifier) : oaiRecord.identifier != null) return false;
        if (dateStamp != null ? !dateStamp.equals(oaiRecord.dateStamp) : oaiRecord.dateStamp != null) return false;
        if (oaiStatus != null ? !oaiStatus.equals(oaiRecord.oaiStatus) : oaiRecord.oaiStatus != null) return false;
        return repositoryId != null ? repositoryId.equals(oaiRecord.repositoryId) : oaiRecord.repositoryId == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (dateStamp != null ? dateStamp.hashCode() : 0);
        result = 31 * result + (oaiStatus != null ? oaiStatus.hashCode() : 0);
        result = 31 * result + (repositoryId != null ? repositoryId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OaiRecord{" +
                "identifier='" + identifier + '\'' +
                ", dateStamp='" + dateStamp + '\'' +
                ", oaiStatus=" + oaiStatus +
                ", repositoryId=" + repositoryId +
                ", processStatus=" + processStatus +
                ", updateCount=" + updateCount +
                '}';
    }

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }
}
