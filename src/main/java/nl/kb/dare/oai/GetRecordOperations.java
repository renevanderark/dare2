package nl.kb.dare.oai;

import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.statuscodes.ErrorStatus;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

class GetRecordOperations {

    private final FileStorage fileStorage;
    private final Consumer<ErrorReport> onError;

    GetRecordOperations(FileStorage fileStorage, Consumer<ErrorReport> onError) {

        this.fileStorage = fileStorage;
        this.onError = onError;
    }

    Optional<FileStorageHandle> getFileStorageHandle(OaiRecord oaiRecord) {
        try {
            return Optional.of(fileStorage.create(oaiRecord));
        } catch (IOException e) {
            onError.accept(new ErrorReport(
                    new IOException("Failed to create storage location for record " + oaiRecord.getIdentifier(), e),
                    ErrorStatus.IO_EXCEPTION)
            );
            return Optional.empty();
        }
    }
}
