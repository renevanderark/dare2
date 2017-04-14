package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OaiRecord {
    private String identifier;
    private String dateStamp;
    private OaiStatus oaiStatus;
    private Integer repositoryId;
    private ProcessStatus processStatus;
    private Integer updateCount = 0;
    private Long totalFileSize = 0L;

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

    public static OaiRecord fromHeader(OaiRecordHeader header, Integer repositoryId) {
        return new OaiRecord(
                header.getIdentifier(),
                header.getDateStamp(),
                header.getOaiStatus(),
                repositoryId,
                header.getOaiStatus() == OaiStatus.AVAILABLE ? ProcessStatus.PENDING : ProcessStatus.SKIP
        );
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

    public Long getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(Long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    @JsonProperty
    public void setOaiStatus(String oaiStatus) {
        this.oaiStatus = OaiStatus.forString(oaiStatus);
    }

    @JsonProperty
    public void setProcessStatus(String processStatus) {
        this.processStatus = ProcessStatus.forString(processStatus);
    }

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }

    public boolean equalsHeader(OaiRecordHeader other, Integer otherRepoId) {
        if (identifier != null ? !identifier.equals(other.getIdentifier()) : other.getIdentifier() != null) return false;
        if (dateStamp != null ? !dateStamp.equals(other.getDateStamp()) : other.getDateStamp() != null) return false;
        if (oaiStatus != null ? !oaiStatus.equals(other.getOaiStatus()) : other.getOaiStatus() != null) return false;
        return repositoryId != null ? repositoryId.equals(otherRepoId) : otherRepoId == null;
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
                ", totalFileSize=" + totalFileSize +
                '}';
    }
}
