package nl.kb.dare.endpoints;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordQuery;
import nl.kb.dare.model.oai.OaiRecordResult;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.oai.GetRecord;
import nl.kb.dare.xslt.XsltTransformer;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Path("/records")
public class OaiRecordsEndpoint {
    private final DBI dbi;
    private final OaiRecordDao oaiRecordDao;
    private final ErrorReportDao errorReportDao;
    private final FileStorage fileStorage;
    private RepositoryDao repositoryDao;
    private HttpFetcher httpFetcher;
    private ResponseHandlerFactory responseHandlerFactory;
    private XsltTransformer xsltTransformer;
    private final FileStorage sampleFileStorage;

    public OaiRecordsEndpoint(DBI dbi, OaiRecordDao oaiRecordDao, ErrorReportDao errorReportDao,
                              FileStorage fileStorage, RepositoryDao repositoryDao,
                              HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                              XsltTransformer xsltTransformer, FileStorage sampleFileStorage) {
        this.dbi = dbi;
        this.oaiRecordDao = oaiRecordDao;
        this.errorReportDao = errorReportDao;
        this.fileStorage = fileStorage;
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.sampleFileStorage = sampleFileStorage;
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

    @GET
    @Produces("application/zip")
    @Path("/{identifier}/download")
    public Response download(@PathParam("identifier") String identifier) {
        final OaiRecord oaiRecord = oaiRecordDao.findByIdentifier(identifier);
        if (oaiRecord == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            final FileStorageHandle fileStorageHandle = fileStorage.create(oaiRecord);
            final StreamingOutput downloadOutput = fileStorageHandle::downloadZip;
            return Response.ok(downloadOutput)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"download.zip\"")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{identifier}/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testGetRecord(@PathParam("identifier") String identifier) {
        final Map<String, Object> result = Maps.newHashMap();

        final List<OaiRecordErrorReport> errors = Lists.newArrayList();
        final OaiRecord oaiRecord = oaiRecordDao.findByIdentifier(identifier);
        if (oaiRecord == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final ProcessStatus andRun = GetRecord.getAndRun(repositoryDao, oaiRecord,
                httpFetcher, responseHandlerFactory,
                sampleFileStorage, xsltTransformer,
                err -> errors.add(new OaiRecordErrorReport(err, oaiRecord)),
                false);

        result.put("errors", errors);
        result.put("result", andRun);
        return Response.ok(result).build();
    }
}
