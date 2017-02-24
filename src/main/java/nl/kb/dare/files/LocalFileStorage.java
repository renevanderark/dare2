package nl.kb.dare.files;

import nl.kb.dare.model.oai.OaiRecord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

class LocalFileStorage implements FileStorage {
    private final String storageDir;

    LocalFileStorage(String storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public FileStorageHandle create(OaiRecord oaiRecord) throws IOException {
        return LocalFileStorageHandle.getInstance(oaiRecord, storageDir)
                .create();
    }

    @Override
    public void purgeRepositoryFiles(Integer id) throws IOException {
        FileUtils.deleteDirectory(new File(String.format("%s/%d", storageDir, id)));
    }
}
