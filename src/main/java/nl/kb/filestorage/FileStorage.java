package nl.kb.filestorage;

import java.io.IOException;

public interface FileStorage {

    FileStorageHandle create(String oaiRecordIdentifier) throws IOException;

    void purgeRepositoryFiles(Integer id) throws IOException;
}
