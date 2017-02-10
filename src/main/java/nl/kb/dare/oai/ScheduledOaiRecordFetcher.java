package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScheduledOaiRecordFetcher extends AbstractScheduledService {
    private static final MessageDigest md5;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final SAXParser saxParser;
    {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiRecordFetcher.class);

    private final OaiRecordDao oaiRecordDao;
    private final RepositoryDao repositoryDao;
    private final ErrorReportDao errorReportDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final FileStorage fileStorage;
    private final XsltTransformer xsltTransformer;

    public ScheduledOaiRecordFetcher(OaiRecordDao oaiRecordDao, RepositoryDao repositoryDao, ErrorReportDao errorReportDao,
                                     HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                     FileStorage fileStorage, XsltTransformer xsltTransformer) {

        this.oaiRecordDao = oaiRecordDao;
        this.repositoryDao = repositoryDao;
        this.errorReportDao = errorReportDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.fileStorage = fileStorage;
        this.xsltTransformer = xsltTransformer;
    }

    @Override
    protected void runOneIteration() throws Exception {


        final Optional<OaiRecord> oaiRecordOptional = fetchNextRecord();
        if (!oaiRecordOptional.isPresent()) { return; }

        final OaiRecord oaiRecord = oaiRecordOptional.get();
        final Repository repositoryConfig = repositoryDao.findById(oaiRecord.getRepositoryId());
        if (repositoryConfig == null) {
            LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", oaiRecord);
            // TODO error report
            finishRecord(oaiRecord, ProcessStatus.FAILED);
            return;
        }

        final Optional<FileStorageHandle> fileStorageHandle = getFileStorageHandle(oaiRecord);
        if (!fileStorageHandle.isPresent()) {
            finishRecord(oaiRecord, ProcessStatus.FAILED);
            return;
        }

        final boolean success = downloadMetadata(oaiRecord, repositoryConfig, fileStorageHandle.get());
        if (!success) {
            finishRecord(oaiRecord, ProcessStatus.FAILED);
            return;
        }


        final boolean allResourcesDownloaded = downloadResources(oaiRecord, repositoryConfig, fileStorageHandle.get());
        if (!allResourcesDownloaded) {
            finishRecord(oaiRecord, ProcessStatus.FAILED);
            return;
        }

        finishRecord(oaiRecord, ProcessStatus.PROCESSED);

    }

    private Optional<FileStorageHandle> getFileStorageHandle(OaiRecord oaiRecord) {
        try {
            return Optional.of(fileStorage.create(oaiRecord));
        } catch (IOException e) {
            LOG.error("Failed to create file storage location", e);
            // TODO
            return Optional.empty();
        }
    }


    private boolean downloadMetadata(OaiRecord oaiRecord, Repository repository, FileStorageHandle fileStorageHandle) {
        try {
            final String urlStr = String.format("%s?verb=GetRecord&metadataPrefix=%s&identifier=%s",
                    repository.getUrl(), repository.getMetadataPrefix(), oaiRecord.getIdentifier());

            final OutputStream out = fileStorageHandle.getOutputStream("metadata.xml");
            LOG.info("fetching record: {}", urlStr);
            final HttpResponseHandler responseHandler = responseHandlerFactory.getXsltTransformingHandler(new StreamResult(out), xsltTransformer);
            httpFetcher.execute(new URL(urlStr), responseHandler);
            responseHandler.getExceptions().forEach(errorReport -> this.saveErrorReport(errorReport, oaiRecord));
            fileStorageHandle.syncFile(out);

            return responseHandler.getExceptions().isEmpty();
        } catch (IOException e) {
            LOG.error("Failed to download metadata", e);
            // TODO
            return false;
        }
    }

    private boolean downloadResources(OaiRecord oaiRecord, Repository repository, FileStorageHandle fileStorageHandle) {
        try {

            final MetsXmlHandler metsXmlHandler = new MetsXmlHandler();
            saxParser.parse(fileStorageHandle.getFile("metadata.xml"), metsXmlHandler);

            final List<String> objectFiles = metsXmlHandler.getObjectFiles();
            if (objectFiles.isEmpty()) {
                LOG.error("No object files provided");
                // TODO error report
                return false;
            }
            final List<ErrorReport> errorReports = Lists.newArrayList();
            for (String objectFile : objectFiles) {
                final URL objectUrl = new URL(objectFile);

                final String filename = FilenameUtils.getName(objectUrl.getPath());
                final String checksumFileName = filename + ".checksum";
                final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
                final OutputStream checksumOut = fileStorageHandle.getOutputStream("resources", checksumFileName);
                final HttpResponseHandler responseHandler = responseHandlerFactory.getStreamCopyingResponseHandler(objectOut, checksumOut);
                httpFetcher.execute(objectUrl, responseHandler);
                errorReports.addAll(responseHandler.getExceptions());
            }
            errorReports.forEach(errorReport -> this.saveErrorReport(errorReport, oaiRecord));
            return errorReports.isEmpty();
        } catch (MalformedURLException e) {
            LOG.error("Url is malformed", e);
            // TODO error report
            return false;
        } catch (IOException e) {
            LOG.error("I/O exception", e);
            // TODO error report
            return false;
        } catch (SAXException e) {
            LOG.error("XML parsing error", e);
            // TODO error report
            return false;
        }

    }

    private Optional<OaiRecord> fetchNextRecord() {
        final OaiRecord oaiRecord = oaiRecordDao.fetchNextWithProcessStatus(ProcessStatus.PENDING.getCode());
        if (oaiRecord == null) {
            return Optional.empty();
        }
        oaiRecord.setProcessStatus(ProcessStatus.PROCESSING);
        oaiRecordDao.update(oaiRecord);
        return Optional.of(oaiRecord);
    }


    private void finishRecord(OaiRecord oaiRecord, ProcessStatus processStatus) {
        oaiRecord.setProcessStatus(processStatus);
        oaiRecordDao.update(oaiRecord);
    }

    private void saveErrorReport(ErrorReport errorReport, OaiRecord oaiRecord) {
        LOG.error("Failed to process record {}", oaiRecord.getIdentifier() ,errorReport.getException());
        errorReportDao.insertOaiRecordError(new OaiRecordErrorReport(errorReport, oaiRecord));
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.MILLISECONDS);
    }
}
