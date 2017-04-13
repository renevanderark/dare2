package nl.kb.dare.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import nl.kb.filestorage.FileStorage;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordQuery;
import nl.kb.dare.model.oai.OaiRecordQueryFactory;
import nl.kb.dare.model.oai.OaiRecordResult;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.oai.GetRecord;
import nl.kb.mets.manifest.ManifestXmlHandler;
import nl.kb.mets.manifest.ObjectResource;
import nl.kb.xslt.XsltTransformer;
import org.glassfish.jersey.server.ChunkedOutput;
import org.skife.jdbi.v2.DBI;
import org.xml.sax.SAXException;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Path("/records")
public class OaiRecordsEndpoint {
    private final DBI dbi;
    private final OaiRecordDao oaiRecordDao;
    private final ErrorReportDao errorReportDao;
    private final FileStorage fileStorage;
    private final OaiRecordQueryFactory oaiRecordQueryFactory;
    private RepositoryDao repositoryDao;
    private HttpFetcher httpFetcher;
    private ResponseHandlerFactory responseHandlerFactory;
    private XsltTransformer xsltTransformer;
    private final FileStorage sampleFileStorage;

    public OaiRecordsEndpoint(DBI dbi,
                              OaiRecordDao oaiRecordDao,
                              ErrorReportDao errorReportDao,
                              OaiRecordQueryFactory oaiRecordQueryFactory,
                              FileStorage fileStorage,
                              RepositoryDao repositoryDao,
                              HttpFetcher httpFetcher,
                              ResponseHandlerFactory responseHandlerFactory,
                              XsltTransformer xsltTransformer,
                              FileStorage sampleFileStorage) {
        this.dbi = dbi;
        this.oaiRecordDao = oaiRecordDao;
        this.errorReportDao = errorReportDao;
        this.oaiRecordQueryFactory = oaiRecordQueryFactory;
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
        final OaiRecordQuery oaiRecordQuery = getOaiRecordQuery(
                repositoryId, offset, limit, processStatusParam, errorStatusParam, oaiStatusParam);

        final OaiRecordResult result = new OaiRecordResult(
                oaiRecordQuery,
                oaiRecordQuery.getResults(dbi),
                oaiRecordQuery.getCount(dbi)
        );

        return Response.ok(result).build();
    }

    @PUT
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bulkReset(
            @QueryParam("repositoryId") Integer repositoryId,
            @QueryParam("processStatus") String processStatusParam,
            @QueryParam("errorStatus") Integer errorStatusParam,
            @QueryParam("oaiStatus") String oaiStatusParam) {

        final OaiRecordQuery oaiRecordQuery = getOaiRecordQuery(
                repositoryId, null, null, processStatusParam, errorStatusParam, oaiStatusParam);

        final List<OaiRecord> results = oaiRecordQuery.getResults(dbi);

        oaiRecordQuery.resetToPending(dbi);

        results.forEach(oaiRecord -> errorReportDao.removeForOaiRecord(oaiRecord.getIdentifier()));


        final OaiRecordResult result = new OaiRecordResult(
                oaiRecordQuery,
                oaiRecordQuery.getResults(dbi),
                oaiRecordQuery.getCount(dbi)
        );

        return Response.ok(result).build();
    }

    private OaiRecordQuery getOaiRecordQuery(Integer repositoryId, Integer offset, Integer limit,
                                             String processStatusParam, Integer errorStatusParam,
                                             String oaiStatusParam) {



        final OaiStatus oaiStatus = OaiStatus.forString(oaiStatusParam);
        final ProcessStatus processStatus = ProcessStatus.forString(processStatusParam);
        final ErrorStatus errorStatus = errorStatusParam == null ?
                null : ErrorStatus.forCode(errorStatusParam);

        return oaiRecordQueryFactory.getInstance(repositoryId, offset, limit, processStatus, oaiStatus, errorStatus);
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

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{identifier}/reset")
    public Response reset(@PathParam("identifier") String identifier) {
        final Map<String, Object> result = Maps.newHashMap();

        final OaiRecord oaiRecord = oaiRecordDao.findByIdentifier(identifier);

        if (oaiRecord == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{}").build();
        }

        if (oaiRecord.getOaiStatus() != OaiStatus.DELETED) {
            oaiRecord.setProcessStatus(ProcessStatus.PENDING);
            errorReportDao.removeForOaiRecord(oaiRecord.getIdentifier());
            oaiRecordDao.update(oaiRecord);
        }

        final List<OaiRecordErrorReport> errorReports = errorReportDao.findByRecordIdentifier(oaiRecord.getIdentifier());
        result.put("record", oaiRecord);
        result.put("errorReports", errorReports);

        return Response.ok(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{identifier}/manifest")
    public Response getManifest(@PathParam("identifier") String identifier) throws JsonProcessingException {
        final OaiRecord oaiRecord = oaiRecordDao.findByIdentifier(identifier);
        if (oaiRecord == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final ProcessStatus processStatus = oaiRecord.getProcessStatus();
        if (processStatus != ProcessStatus.PROCESSED && processStatus != ProcessStatus.FAILED) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            final FileStorageHandle fileStorageHandle = fileStorage.create(oaiRecord.getIdentifier());
            final InputStream manifest = processStatus == ProcessStatus.PROCESSED ?
                    fileStorageHandle.getFile("manifest.xml") :
                    fileStorageHandle.getFile("manifest.initial.xml");

            final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            final ManifestXmlHandler manifestXmlHandler = new ManifestXmlHandler();
            saxParser.parse(manifest, manifestXmlHandler);

            final List<ObjectResource> response = manifestXmlHandler
                    .getObjectResourcesIncludingMetadata()
                    .stream()
                    .map(objectResource -> {
                        try {
                            final String xlinkHref = objectResource
                                .getXlinkHref()
                                .replace("file://./", String.format("/records/%s/download/",
                                        URLEncoder.encode(identifier, "utf8")));
                            objectResource.setXlinkHref(xlinkHref);
                            return objectResource;
                        } catch (UnsupportedEncodingException e) {
                            return objectResource;
                        }

                    }).collect(Collectors.toList());
            return Response.ok(response).build();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ObjectMapper().writeValueAsString(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{identifier}/test")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response testGetRecord(@PathParam("identifier") String identifier) {

        final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);
        final OaiRecord oaiRecord = oaiRecordDao.findByIdentifier(identifier);
        if (oaiRecord == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        new Thread(() -> {

            final ProcessStatus result = GetRecord.getAndRun(repositoryDao, oaiRecord,
                    httpFetcher, responseHandlerFactory,
                    sampleFileStorage, xsltTransformer,
                    err -> writeChunk(output, new OaiRecordErrorReport(err, oaiRecord), false),
                    progressReport -> writeChunk(output, progressReport, false),
                    false);

            final Map<String, ProcessStatus> resultMap = Maps.newHashMap();
            resultMap.put("result", result);
            writeChunk(output, resultMap, true);
        }).start();

        return Response
                .ok(output)
                .header(HttpHeaders.CONTENT_ENCODING, "identity")
                .build();
    }

    private void writeChunk(ChunkedOutput<String> output, Object progressReport, boolean andClose) {
        try {
            output.write("\n" +
                new ObjectMapper().writeValueAsString(progressReport) + "!--end-chunk--!\n"
            );
            if (andClose) { output.close(); }
        } catch (IOException ignored) {

        }
    }
}
