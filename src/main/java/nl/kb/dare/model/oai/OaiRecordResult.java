package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OaiRecordResult {
    private final OaiRecordQuery oaiRecordQuery;
    private final List<OaiRecord> result;
    private final Long count;

    public OaiRecordResult(OaiRecordQuery oaiRecordQuery, List<OaiRecord> result, Long count) {
        this.oaiRecordQuery = oaiRecordQuery;
        this.result = result;
        this.count = count;
    }

    @JsonProperty
    public OaiRecordQuery getOaiRecordQuery() {
        return oaiRecordQuery;
    }

    @JsonProperty
    public List<OaiRecord> getResult() {
        return result;
    }

    @JsonProperty
    public Long getCount() {
        return count;
    }
}
