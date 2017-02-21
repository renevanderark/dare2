package nl.kb.dare.endpoints;

import com.google.common.collect.Maps;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordQuery;
import nl.kb.dare.model.oai.OaiRecordResult;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;


@Path("/records")
public class OaiRecordsEndpoint {
    private final DBI dbi;
    private final OaiRecordDao oaiRecordDao;
    private final ErrorReportDao errorReportDao;

    public OaiRecordsEndpoint(DBI dbi, OaiRecordDao oaiRecordDao, ErrorReportDao errorReportDao) {
        this.dbi = dbi;
        this.oaiRecordDao = oaiRecordDao;
        this.errorReportDao = errorReportDao;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(
            @QueryParam("repositoryId") Integer repositoryId,
            @QueryParam("offset") Integer offsetParam,
            @QueryParam("limit") Integer limitParam,
            @QueryParam("processStatus") String processStatusParam,
            @QueryParam("errorStatus") Integer errorStatusParam,
            @QueryParam("oaiStatus") String oaiStatusParam) {

        final Integer offset = offsetParam == null ? 0 : offsetParam;
        final Integer limit = limitParam == null ? 10 : limitParam;

        final OaiStatus oaiStatus = OaiStatus.forString(oaiStatusParam);
        final ProcessStatus processStatus = ProcessStatus.forString(processStatusParam);
        final ErrorStatus errorStatus = errorStatusParam == null ?
                null : ErrorStatus.forCode(errorStatusParam);

        final OaiRecordQuery oaiRecordQuery = new OaiRecordQuery(repositoryId, offset, limit, processStatus, oaiStatus, errorStatus);
        final OaiRecordResult result = new OaiRecordResult(
                oaiRecordQuery,
                oaiRecordQuery.getResults(dbi),
                oaiRecordQuery.getCount(dbi)
        );

        return Response.ok(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{identifier}")
    public Response get(@PathParam("identifier") String identifier) {
        final Map<String, Object> result = Maps.newHashMap();

        final OaiRecord oaiRecord = oaiRecordDao.findByIdentifier(identifier);

        if (oaiRecord == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{}").build();
        }

        final List<OaiRecordErrorReport> errorReports = errorReportDao.findByRecordIdentifier(oaiRecord.getIdentifier());

        result.put("record", oaiRecord);
        result.put("errorReports", errorReports);

        return Response.ok(result).build();
    }
}
