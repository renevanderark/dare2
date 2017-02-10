package nl.kb.dare.files;

import nl.kb.dare.model.oai.OaiRecord;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LocalFileStorageHandleTest {

    private static final String IDENTIFIER = "identifier";
    private static final int REPOSITORY_ID = 1;
    private static final String DATE_STAMP = "2015-01-01T01:02:05Z";
    private static final String BASE_PATH = "./test";
    private MessageDigest md5;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        md5 = MessageDigest.getInstance("MD5");
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(BASE_PATH));
    }

    @Test
    public void createShouldCreateANewDirectory() throws IOException {
        final OaiRecord oaiRecord = new OaiRecord();
        oaiRecord.setRepositoryId(REPOSITORY_ID);
        oaiRecord.setIdentifier(IDENTIFIER);
        oaiRecord.setDateStamp(DATE_STAMP);
        final LocalFileStorageHandle instance = LocalFileStorageHandle.getInstance(oaiRecord, BASE_PATH);

        instance.create();

        final File expectedDir = new File(LocalFileStorageHandle.getFilePath(oaiRecord, BASE_PATH));

        assertThat(expectedDir.exists(), is(true));
        assertThat(expectedDir.isDirectory(), is(true));
    }

    @Test
    public void clearShouldRecursivelyDeleteContents() throws IOException {
        final OaiRecord oaiRecord = new OaiRecord();
        oaiRecord.setRepositoryId(REPOSITORY_ID);
        oaiRecord.setIdentifier(IDENTIFIER);
        oaiRecord.setDateStamp(DATE_STAMP);
        final LocalFileStorageHandle instance = LocalFileStorageHandle.getInstance(oaiRecord, BASE_PATH);

        instance.create();

        final String filePath = LocalFileStorageHandle.getFilePath(oaiRecord, BASE_PATH);

        final String testSubdirPath = String.format("%s/%s", filePath, "testing");
        final String testFilePath = String.format("%s/%s", testSubdirPath, "test1");
        new File(testSubdirPath).mkdirs();
        FileUtils.touch(new File(testFilePath));

        instance.clear();

        assertThat(new File(testFilePath).exists(), is(false));
        assertThat(new File(testSubdirPath).exists(), is(false));
    }
}