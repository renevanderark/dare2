package nl.kb.dare.model.oai;

import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.oaipmh.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;

public class OaiRecordQueryFactory {

    private final String databaseProvider;

    public OaiRecordQueryFactory(String databaseProvider) {

        this.databaseProvider = databaseProvider;
    }

    public OaiRecordQuery getInstance(Integer repositoryId, Integer offset, Integer limit, ProcessStatus processStatus,
                                      OaiStatus oaiStatus, ErrorStatus errorStatus) {
        return new OaiRecordQuery(repositoryId, offset, limit, processStatus, oaiStatus, errorStatus, databaseProvider);
    }

    public OaiRecordQuery getInstance(ProcessStatus processStatus) {
        return getInstance(null, null,null,processStatus,null, null);
    }
}
