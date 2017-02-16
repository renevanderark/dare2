package nl.kb.dare.endpoints;

import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordQuery;
import nl.kb.dare.model.oai.OaiRecordResult;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


@Path("/repositories/{repositoryId}/records")
public class OaiRecordsEndpoint {
    private final OaiRecordDao oaiRecordDao;

    public OaiRecordsEndpoint(OaiRecordDao oaiRecordDao) {
        this.oaiRecordDao = oaiRecordDao;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(
            @PathParam("repositoryId") Integer repositoryId,
            @QueryParam("offset") Integer offsetParam,
            @QueryParam("limit") Integer limitParam,
            @QueryParam("processStatus") String processStatusParam,
            @QueryParam("oaiStatus") String oaiStatusParam) {

        final Integer offset = offsetParam == null ? 0 : offsetParam;
        final Integer limit = limitParam == null ? 10 : limitParam;

        final OaiStatus oaiStatus = OaiStatus.forString(oaiStatusParam);
        final ProcessStatus processStatus = ProcessStatus.forString(processStatusParam);

        final OaiRecordResult result = new OaiRecordResult(
                new OaiRecordQuery(repositoryId, offset, limit, processStatus, oaiStatus),
                getResult(repositoryId, offset, limit, processStatus, oaiStatus),
                getCount(repositoryId, processStatus, oaiStatus)
        );

        return Response.ok(result).build();
    }

    private Long getCount(Integer repositoryId, ProcessStatus processStatus, OaiStatus oaiStatus) {
        if (processStatus != null && oaiStatus != null) {
            return oaiRecordDao.countWithProcessStatusAndOaiStatus(repositoryId, processStatus.getCode(), oaiStatus.getCode());
        } else if (processStatus != null) {
            return oaiRecordDao.countWithProcessStatus(repositoryId, processStatus.getCode());
        } else if (oaiStatus != null) {
            return oaiRecordDao.countWithOaiStatus(repositoryId, oaiStatus.getCode());
        } else {
            return oaiRecordDao.count(repositoryId);
        }
    }

    private List<OaiRecord> getResult(Integer repositoryId, Integer offset, Integer limit, ProcessStatus processStatus, OaiStatus oaiStatus) {
        if (processStatus != null && oaiStatus != null) {
            return oaiRecordDao.listWithProcessStatusAndOaiStatus(repositoryId, offset, limit, processStatus.getCode(), oaiStatus.getCode());
        } else if (processStatus != null) {
            return oaiRecordDao.listWithProcessStatus(repositoryId, offset, limit, processStatus.getCode());
        } else if (oaiStatus != null) {
            return oaiRecordDao.listWithOaiStatus(repositoryId, offset, limit, oaiStatus.getCode());
        } else {
            return oaiRecordDao.list(repositoryId, offset, limit);
        }
    }
}
