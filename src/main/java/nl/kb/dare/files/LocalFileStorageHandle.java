package nl.kb.dare.files;

import nl.kb.dare.model.oai.OaiRecord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import static nl.kb.dare.checksum.ChecksumUtil.getChecksumString;

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

    static synchronized LocalFileStorageHandle getInstance(OaiRecord oaiRecord, String basePath) {
        final String filePath = getFilePath(oaiRecord, basePath);
        if (!instances.containsKey(filePath)) {
            instances.put(filePath, new LocalFileStorageHandle(filePath));
        }
        return instances.get(filePath);
    }

    static String getFilePath(OaiRecord oaiRecord, String basePath) {
        final Integer repositoryId = oaiRecord.getRepositoryId();
        final String dateStampPart = oaiRecord.getDateStamp().substring(0, 13);
        final String idPart = getChecksumString(md5.digest(oaiRecord.getIdentifier().getBytes()));
        return String.format("%s/%d/%s/%s", basePath, repositoryId, dateStampPart, idPart);
    }

    @Override
    public FileStorageHandle create() throws IOException {
        FileUtils.forceMkdir(new File(fileDir));
        return this;
    }

    @Override
    public FileStorageHandle clear() throws IOException {
        FileUtils.cleanDirectory(new File(fileDir));
        return this;
    }

    @Override
    public OutputStream getOutputStream(String filename) throws IOException {
        return new FileOutputStream(new File(String.format("%s/%s", fileDir, filename)));
    }

    @Override
    public OutputStream getOutputStream(String path, String filename) throws IOException {
        final String filePath = String.format("%s/%s", fileDir, path);
        FileUtils.forceMkdir(new File(filePath));
        return new FileOutputStream(new File(String.format("%s/%s", filePath, filename)));
    }

    @Override
    public InputStream getFile(String filename) throws FileNotFoundException {
        return new FileInputStream(new File(String.format("%s/%s", fileDir, filename)));
    }

    @Override
    public void syncFile(OutputStream out) throws IOException {
        ((FileOutputStream) out).getFD().sync();
    }

    @Override
    public void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(new File(fileDir));
    }
}
