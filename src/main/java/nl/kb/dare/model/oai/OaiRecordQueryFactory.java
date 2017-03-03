package nl.kb.dare.model.oai;

import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;

public class OaiRecordQueryFactory {
    public OaiRecordQuery getInstance(Integer repositoryId, Integer offset, Integer limit, ProcessStatus processStatus,
                                      OaiStatus oaiStatus, ErrorStatus errorStatus) {
        return new OaiRecordQuery(repositoryId, offset, limit, processStatus, oaiStatus, errorStatus);
    }

    public OaiRecordQuery  getInstance(ProcessStatus processStatus) {
        return getInstance(null, null,null,processStatus,null, null);
    }
}
