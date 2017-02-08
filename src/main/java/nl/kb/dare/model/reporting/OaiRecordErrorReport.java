package nl.kb.dare.model.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OaiRecordErrorReport extends StorableErrorReport {

    private String recordIdentifier;

    public OaiRecordErrorReport(String message, String filteredStackTrace, String dateStamp, String url, String recordIdentifier) {
        super(message, filteredStackTrace, dateStamp, url);
        this.recordIdentifier = recordIdentifier;
    }

    @JsonProperty
    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }
}
