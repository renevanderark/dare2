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
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LocalFileStorageHandleTest {

    public static final String IDENTIFIER = "identifier";
    public static final int REPOSITORY_ID = 1;
    public static final String DATE_STAMP = "2015-01-01T01:02:05Z";
    private MessageDigest md5;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        md5 = MessageDigest.getInstance("MD5");
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File("./test"));
    }

    @Test
    public void createShouldCreateANewDirectory() throws IOException {
        final String identifier = UUID.randomUUID().toString();
        final OaiRecord oaiRecord = new OaiRecord();
        oaiRecord.setRepositoryId(REPOSITORY_ID);
        oaiRecord.setIdentifier(IDENTIFIER);
        oaiRecord.setDateStamp(DATE_STAMP);
        final LocalFileStorageHandle instance = LocalFileStorageHandle.getInstance(oaiRecord, "./test");

        instance.create();

        final File expectedDir = new File(LocalFileStorageHandle.getFilePath(oaiRecord, "./test"));

        assertThat(expectedDir.exists(), is(true));
        assertThat(expectedDir.isDirectory(), is(true));
    }
}