package nl.kb.dare.endpoints;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordQuery;
import nl.kb.dare.model.oai.OaiRecordResult;

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
    private static JsonNodeFactory json = JsonNodeFactory.instance;
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
            @QueryParam("processStatus") String processStatus,
            @QueryParam("oaiStatus") String oaiStatus) {

        final Integer offset = offsetParam == null ? 0 : offsetParam;
        final Integer limit = limitParam == null ? 10 : limitParam;


        final OaiRecordResult result = new OaiRecordResult(
                new OaiRecordQuery(repositoryId, offset, limit, processStatus, oaiStatus),
                getResult(repositoryId, offset, limit, processStatus, oaiStatus),
                getCount(repositoryId, processStatus, oaiStatus)
        );

        return Response.ok(result).build();
    }

    private Long getCount(Integer repositoryId, String processStatus, String oaiStatus) {
        if (processStatus != null && oaiStatus != null) {
            return oaiRecordDao.countWithProcessStatusAndOaiStatus(repositoryId, processStatus, oaiStatus);
        } else if (processStatus != null) {
            return oaiRecordDao.countWithProcessStatus(repositoryId, processStatus);
        } else if (oaiStatus != null) {
            return oaiRecordDao.countWithOaiStatus(repositoryId, oaiStatus);
        } else {
            return oaiRecordDao.count(repositoryId);
        }
    }

    private List<OaiRecord> getResult(@PathParam("repositoryId") Integer repositoryId, Integer offset, Integer limit, String processStatus, String oaiStatus) {
        if (processStatus != null && oaiStatus != null) {
            return oaiRecordDao.listWithProcessStatusAndOaiStatus(repositoryId, offset, limit, processStatus, oaiStatus);
        } else if (processStatus != null) {
            return oaiRecordDao.listWithProcessStatus(repositoryId, offset, limit, processStatus);
        } else if (oaiStatus != null) {
            return oaiRecordDao.listWithOaiStatus(repositoryId, offset, limit, oaiStatus);
        } else {
            return oaiRecordDao.list(repositoryId, offset, limit);
        }
    }
}
