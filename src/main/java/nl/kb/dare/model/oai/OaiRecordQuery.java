package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;

public class OaiRecordQuery {
    private final Integer repositoryId;
    private final Integer offset;
    private final Integer limit;
    private final ProcessStatus processStatus;
    private final OaiStatus oaiStatus;

    public OaiRecordQuery(Integer repositoryId, Integer offset, Integer limit, ProcessStatus processStatus, OaiStatus oaiStatus) {

        this.repositoryId = repositoryId;
        this.offset = offset;
        this.limit = limit;
        this.processStatus = processStatus;
        this.oaiStatus = oaiStatus;
    }

    @JsonProperty
    public Integer getRepositoryId() {
        return repositoryId;
    }

    @JsonProperty
    public Integer getOffset() {
        return offset;
    }

    @JsonProperty
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty
    public String getProcessStatus() {
        return processStatus != null ? processStatus.getStatus() : "<< no supported parameter defined >>";
    }

    @JsonProperty
    public String getOaiStatus() {
        return oaiStatus != null ? oaiStatus.getStatus() : "<< no supported parameter defined >>";
    }
}
