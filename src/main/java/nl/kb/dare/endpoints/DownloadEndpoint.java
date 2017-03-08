package nl.kb.dare.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.oai.ManifestXmlHandler;
import nl.kb.dare.oai.ObjectResource;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

@Path("/records/{identifier}/download")
public class DownloadEndpoint {
    private final OaiRecordDao oaiRecordDao;
    private final FileStorage fileStorage;

    public DownloadEndpoint(OaiRecordDao oaiRecordDao, FileStorage fileStorage) {
        this.oaiRecordDao = oaiRecordDao;
        this.fileStorage = fileStorage;
    }

    @GET
    @Produces("application/zip")
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
    @Path("/{filename}")
    public Response download(
            @PathParam("identifier") String identifier,
            @PathParam("filename") String filename) throws JsonProcessingException {

        return downloadResourceFile(identifier, filename);
    }

    @GET
    @Path("/{path}/{filename}")
    public Response download(
            @PathParam("identifier") String identifier,
            @PathParam("path") String path,
            @PathParam("filename") String filename) throws JsonProcessingException {

        return downloadResourceFile(identifier, String.format("%s/%s", path, filename));
    }


    private Response downloadResourceFile(String identifier, String filename) throws JsonProcessingException {
        final OaiRecord oaiRecord = oaiRecordDao.findByIdentifier(identifier);
        if (oaiRecord == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (oaiRecord.getProcessStatus() != ProcessStatus.PROCESSED) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            final FileStorageHandle fileStorageHandle = fileStorage.create(oaiRecord);
            final InputStream manifest = fileStorageHandle.getFile("manifest.xml");

            final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            final ManifestXmlHandler manifestXmlHandler = new ManifestXmlHandler();
            saxParser.parse(manifest, manifestXmlHandler);
            final Optional<ObjectResource> resource = manifestXmlHandler
                    .getObjectResourcesIncludingMetadata()
                    .stream()
                    .filter(res -> {
                        try {
                            return URLDecoder.decode(res.getXlinkHref()
                                    .replace("file://./", ""), "UTF8")
                                    .equals(filename);
                        } catch (UnsupportedEncodingException e) {
                            return false;
                        }
                    }).findFirst();
            if (!resource.isPresent()) {
                throw new IOException("Resource not found in manifest: " + identifier + " - " + filename);
            }

            final InputStream file = fileStorageHandle.getFile(filename);
            final StreamingOutput downloadOutput = out -> IOUtils.copy(file, out);

            final long size = resource.get().getSize();

            return Response.ok(downloadOutput)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                            filename.replaceAll("^.+\\/", "") + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, size)
                    .build();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ObjectMapper().writeValueAsString(e.getMessage()))
                    .build();
        }
    }
}
