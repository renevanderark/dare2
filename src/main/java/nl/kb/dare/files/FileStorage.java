package nl.kb.dare.files;

import java.io.IOException;

public interface FileStorage {

    FileStorageHandle create(String oaiRecordIdentifier) throws IOException;

    void purgeRepositoryFiles(Integer id) throws IOException;
}
