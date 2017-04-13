package nl.kb.dare.files;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LocalFileStorageHandleTest {

    private static final String IDENTIFIER = "identifier";
    private static final String BASE_PATH = "./test";
    private LocalFileStorageHandle instance;

    @Before
    public void setUp() {
        instance = LocalFileStorageHandle.getInstance(IDENTIFIER, BASE_PATH);
    }
    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(BASE_PATH));
    }

    @Test
    public void createShouldCreateANewDirectory() throws IOException {


        instance.create();

        final File expectedDir = new File(LocalFileStorageHandle.getFilePath(IDENTIFIER, BASE_PATH));

        assertThat(expectedDir.exists(), is(true));
        assertThat(expectedDir.isDirectory(), is(true));
    }

    @Test
    public void clearShouldRecursivelyDeleteContents() throws IOException {

        instance.create();

        final String filePath = LocalFileStorageHandle.getFilePath(IDENTIFIER, BASE_PATH);

        final String testSubdirPath = String.format("%s/%s", filePath, "testing");
        final String testFilePath = String.format("%s/%s", testSubdirPath, "test1");
        new File(testSubdirPath).mkdirs();
        FileUtils.touch(new File(testFilePath));

        instance.clear();

        assertThat(new File(testFilePath).exists(), is(false));
        assertThat(new File(testSubdirPath).exists(), is(false));
    }

    @Test
    public void getOutputStreamPointsToTheRequestedFile() throws IOException {
        final OutputStream outputStream = instance.create().getOutputStream("test.foo");
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, Charset.forName("UTF8")));
        final String filePath = LocalFileStorageHandle.getFilePath(IDENTIFIER, BASE_PATH);

        printWriter.print("testing");
        printWriter.flush();
        printWriter.close();

        final String data = FileUtils.readFileToString(new File(String.format("%s/%s", filePath, "test.foo")), Charset.forName("UTF8"));

        assertThat(data, is("testing"));
    }

    @Test
    public void getOutputStreamPointsToTheRequestedFileAndSubdir() throws IOException {
        final OutputStream outputStream = instance.create().getOutputStream("sub/dir","test.foo");
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, Charset.forName("UTF8")));

        final String filePath = LocalFileStorageHandle.getFilePath(IDENTIFIER, BASE_PATH);

        printWriter.print("testing");
        printWriter.flush();
        printWriter.close();

        final String data = FileUtils.readFileToString(new File(String.format("%s/sub/dir/%s", filePath, "test.foo")), Charset.forName("UTF8"));

        assertThat(data, is("testing"));
    }

}