package nl.kb.dare.oai;

import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ProgressReport;
import nl.kb.dare.model.reporting.progress.GetRecordProgressReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static nl.kb.dare.model.reporting.progress.GetRecordProgressReport.ProgressStep.COLLECT_RESOURCES;
import static nl.kb.dare.model.reporting.progress.GetRecordProgressReport.ProgressStep.DOWNLOAD_METADATA;
import static nl.kb.dare.model.reporting.progress.GetRecordProgressReport.ProgressStep.DOWNLOAD_RESOURCES;
import static nl.kb.dare.model.reporting.progress.GetRecordProgressReport.ProgressStep.FINALIZE_MANIFEST;
import static nl.kb.dare.model.reporting.progress.GetRecordProgressReport.ProgressStep.GENERATE_MANIFEST;


public class GetRecord {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecord.class);


    private final OaiRecord oaiRecord;
    private final Consumer<ProgressReport> onProgress;
    private final boolean inSampleMode;
    private final GetRecordOperations getRecordOperations;

    GetRecord(GetRecordOperations getRecordOperations, OaiRecord oaiRecord, Consumer<ProgressReport> onProgress,
              boolean inSampleMode) {
        this.getRecordOperations = getRecordOperations;
        this.oaiRecord = oaiRecord;
        this.onProgress = onProgress;
        this.inSampleMode = inSampleMode;
    }

    public static ProcessStatus getAndRun(RepositoryDao repositoryDao, OaiRecord oaiRecord,
                                          HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                          FileStorage fileStorage, XsltTransformer xsltTransformer,
                                          Consumer<ErrorReport> onError,
                                          Consumer<ProgressReport> onProgress,
                                          boolean inSampleMode) {

        final Repository repositoryConfig = repositoryDao.findById(oaiRecord.getRepositoryId());
        if (repositoryConfig == null) {
            LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", oaiRecord);
            // TODO error report
            return ProcessStatus.FAILED;
        }

        final GetRecordResourceOperations resourceOperations = new GetRecordResourceOperations(
                httpFetcher, responseHandlerFactory, onProgress);

        final GetRecordOperations getRecordOperations = new GetRecordOperations(
                fileStorage, httpFetcher, responseHandlerFactory, xsltTransformer,
                repositoryConfig, resourceOperations, new ManifestFinalizer(),
                onError, onProgress);

        return new GetRecord(getRecordOperations, oaiRecord, onProgress, inSampleMode).fetch();
    }

    ProcessStatus fetch() {

        final Optional<FileStorageHandle> fileStorageHandle = getRecordOperations.getFileStorageHandle(oaiRecord);
        if (!fileStorageHandle.isPresent()) {
            onProgress.accept(new GetRecordProgressReport(DOWNLOAD_METADATA, false));
            return ProcessStatus.FAILED;
        }

        final FileStorageHandle handle = fileStorageHandle.get();
        final Optional<ObjectResource> metadataResource = getRecordOperations.downloadMetadata(handle, oaiRecord);
        if (!metadataResource.isPresent()) {
            onProgress.accept(new GetRecordProgressReport(DOWNLOAD_METADATA, false));
            return ProcessStatus.FAILED;
        }
        onProgress.accept(new GetRecordProgressReport(DOWNLOAD_METADATA, true));

        if (!getRecordOperations.generateManifest(handle)) {
            onProgress.accept(new GetRecordProgressReport(GENERATE_MANIFEST, false));
            return ProcessStatus.FAILED;
        }
        onProgress.accept(new GetRecordProgressReport(GENERATE_MANIFEST, true));


        final List<ObjectResource> objectResources = getRecordOperations.collectResources(handle);
        if (objectResources.isEmpty()) {
            onProgress.accept(new GetRecordProgressReport(COLLECT_RESOURCES, false));
            return ProcessStatus.FAILED;
        }
        onProgress.accept(new GetRecordProgressReport(COLLECT_RESOURCES, true));


        if (!getRecordOperations.downloadResources(handle, objectResources)) {
            onProgress.accept(new GetRecordProgressReport(DOWNLOAD_RESOURCES, false));
            return ProcessStatus.FAILED;
        }
        onProgress.accept(new GetRecordProgressReport(DOWNLOAD_RESOURCES, true));


        if (!getRecordOperations.writeFilenamesAndChecksumsToMetadata(handle, objectResources, metadataResource.get())) {
            onProgress.accept(new GetRecordProgressReport(FINALIZE_MANIFEST, false));
            return ProcessStatus.FAILED;
        }
        onProgress.accept(new GetRecordProgressReport(FINALIZE_MANIFEST, true));

        if (inSampleMode) {
            try {
                handle.deleteFiles();
            } catch (IOException e) {
                LOG.error("Failure trying to delete downloaded files while in sample mode", e);
            }
        }
        return ProcessStatus.PROCESSED;
    }
}
