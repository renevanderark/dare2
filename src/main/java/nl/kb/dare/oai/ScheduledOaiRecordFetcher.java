package nl.kb.dare.oai;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ScheduledOaiRecordFetcher extends AbstractScheduledService {
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

        // We do not want to free the dao until processing updates are finished on the record
        synchronized (oaiRecordDao) {

            final Optional<OaiRecord> oaiRecordOptional = fetchNextRecord();
            if (!oaiRecordOptional.isPresent()) { return; }

            final OaiRecord oaiRecord = oaiRecordOptional.get();
            final Repository repositoryConfig = repositoryDao.findById(oaiRecord.getRepositoryId());
            if (repositoryConfig == null) {
                LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", oaiRecord);
                // TODO error report
                return;
            }

            downloadRecordMetadata(oaiRecord, repositoryConfig);

            finishRecord(oaiRecord);
        }
    }

    private void downloadRecordMetadata(OaiRecord oaiRecord, Repository repository) {
        try {
            final String urlStr = String.format("%s?verb=GetRecord&metadataPrefix=%s&identifier=%s",
                    repository.getUrl(), repository.getMetadataPrefix(), oaiRecord.getIdentifier());

            final FileStorageHandle fileStorageHandle = fileStorage.create(oaiRecord);
            final OutputStream out = fileStorageHandle.getOutputStream("metadata.xml");
            final PipedInputStream pipe = new PipedInputStream();
            LOG.info("fetching record: {}", urlStr);

            final HttpResponseHandler responseHandler = responseHandlerFactory.getXsltTransformingHandler(new StreamResult(out), xsltTransformer);

            httpFetcher.execute(new URL(urlStr), responseHandler);
            responseHandler.throwAnyException();

        } catch (MalformedURLException e) {
            LOG.error("Url is malformed", e);
            // TODO error report
        } catch (IOException e) {
            LOG.error("I/O exception", e);
            // TODO error report
        } catch (SAXException e) {
            LOG.error("XML parsing error", e);
            // TODO error report
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


    private void finishRecord(OaiRecord oaiRecord) {
        oaiRecord.setProcessStatus(ProcessStatus.PROCESSED);
        oaiRecordDao.update(oaiRecord);
    }


    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.MILLISECONDS);
    }
}
