package nl.kb.dare.model.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HarvesterErrorReport extends StorableErrorReport {

    private Integer repositoryId;

    public HarvesterErrorReport(ErrorReport errorReport, Integer repositoryId) {
        super(errorReport);
        this.repositoryId = repositoryId;
    }

    @JsonProperty
    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }
}
