package nl.kb.dare.model.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.statuscodes.ErrorStatus;

public class OaiRecordErrorReport extends StorableErrorReport {

    private String recordIdentifier;

    public OaiRecordErrorReport(String message, String filteredStackTrace, String dateStamp, String url, ErrorStatus errorStatus, String recordIdentifier) {
        super(message, filteredStackTrace, dateStamp, url, errorStatus);
        this.recordIdentifier = recordIdentifier;
    }

    public OaiRecordErrorReport(ErrorReport errorReport, OaiRecord oaiRecord) {
        super(errorReport);
        this.recordIdentifier = oaiRecord.getIdentifier();
    }

    @JsonProperty
    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }
}
