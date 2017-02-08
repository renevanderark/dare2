package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OaiRecordQuery {
    private final Integer repositoryId;
    private final Integer offset;
    private final Integer limit;
    private final String processStatus;
    private final String oaiStatus;

    public OaiRecordQuery(Integer repositoryId, Integer offset, Integer limit, String processStatus, String oaiStatus) {

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
        return processStatus;
    }

    @JsonProperty
    public String getOaiStatus() {
        return oaiStatus;
    }
}
