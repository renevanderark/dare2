package nl.kb.dare.files;

import nl.kb.dare.model.oai.OaiRecord;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

class LocalFileStorageHandle implements FileStorageHandle {
    private static final int MAX_ENTRIES = 10_000;
    private static final MessageDigest md5;
    private static final LinkedHashMap<String, LocalFileStorageHandle> instances = new LinkedHashMap<String, LocalFileStorageHandle>(){
        protected boolean removeEldestEntry(Map.Entry<String, LocalFileStorageHandle> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final String fileDir;

    private LocalFileStorageHandle(String fileDir) {
        this.fileDir = fileDir;
    }

    static LocalFileStorageHandle getInstance(OaiRecord oaiRecord, String basePath) {
        final String filePath = getFilePath(oaiRecord, basePath);
        if (!instances.containsKey(filePath)) {
            instances.put(filePath, new LocalFileStorageHandle(filePath));
        }
        return instances.get(filePath);
    }

    static String getFilePath(OaiRecord oaiRecord, String basePath) {
        final Integer repositoryId = oaiRecord.getRepositoryId();
        final String dateStampPart = oaiRecord.getDateStamp().substring(0, 13);
        final String idPart = (new HexBinaryAdapter()).marshal((md5.digest(oaiRecord.getIdentifier().getBytes())));
        return String.format("%s/%d/%s/%s", basePath, repositoryId, dateStampPart, idPart);
    }

    FileStorageHandle create() throws IOException {
        final File fileDirF = new File(fileDir);
        if (!fileDirF.exists()) {
            final boolean mkdirSucceeded = fileDirF.mkdirs();
            if (!mkdirSucceeded) {
                throw new IOException("could not create local directory: " + fileDir);
            }
        }
        return this;
    }
}