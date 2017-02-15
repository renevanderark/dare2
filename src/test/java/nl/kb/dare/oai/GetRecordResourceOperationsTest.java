package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.reporting.ErrorReport;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetRecordResourceOperationsTest {

    private static final String BASE_URL = "http://example.com/path/";
    private static final String ORIG_ENCODED_FILENAME = "file%201.ext";
    private static final String FULL_URL = BASE_URL + ORIG_ENCODED_FILENAME;
    private static final String EXPECTED_FILENAME = "file 1.ext";
    private static final String TRANSFORMED_ENC_FILE_1 = "file+1.ext";
    private static final String TRANSFORMED_ENC_FILE_2 = "file%201.ext";

    @Test
    public void downloadResourceShouldSaveTheFileAndTheChecksum() throws IOException {
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final ObjectResource objectResource = getObjectResource(FULL_URL);
        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final GetRecordResourceOperations instance = new GetRecordResourceOperations(httpFetcher, responseHandlerFactory);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        when(responseHandlerFactory.getStreamCopyingResponseHandler(any(), any()))
                .thenReturn(responseHandler);
        when(responseHandler.getExceptions()).thenReturn(Lists.newArrayList());


        final List<ErrorReport> errorReports = instance.downloadResource(objectResource, fileStorageHandle);


        InOrder inOrder = Mockito.inOrder(objectResource, fileStorageHandle, responseHandlerFactory, httpFetcher);
        // final String fileLocation = objectResource.getXlinkHref();
        inOrder.verify(objectResource).getXlinkHref();
        // final String filename = createFilename(fileLocation); => EXPECTED_FILENAME
        // final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
        inOrder.verify(fileStorageHandle).getOutputStream("resources", EXPECTED_FILENAME);
        // final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();
        // final List<ErrorReport> firstAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, false);
        inOrder.verify(responseHandlerFactory).getStreamCopyingResponseHandler(any(), any());
        inOrder.verify(httpFetcher).execute(
                argThat(allOf(
                    hasProperty("host", is("example.com")),
                    hasProperty("file", is("/path/" + TRANSFORMED_ENC_FILE_1))
                )),
                argThat(is(responseHandler))
        );
        // if (firstAttemptErrors.isEmpty()) {
        // .. writeChecksum(objectResource, checksumOut);
        inOrder.verify(objectResource).setChecksum(argThat(is(instanceOf(String.class))));
        inOrder.verify(objectResource).setChecksumType(argThat(is("MD5")));

        // .. return Lists.newArrayList();
        assertThat(errorReports.isEmpty(), is(true));
        // }
    }

    @Test
    public void downloadResourceShouldSaveTheFileAndTheChecksumAfterSecondAttempt() throws IOException {
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final ObjectResource objectResource = getObjectResource(FULL_URL);
        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final GetRecordResourceOperations instance = new GetRecordResourceOperations(httpFetcher, responseHandlerFactory);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        when(responseHandlerFactory.getStreamCopyingResponseHandler(any(), any()))
                .thenReturn(responseHandler);
        when(responseHandler.getExceptions())
                .thenReturn(Lists.newArrayList(mock(ErrorReport.class)))
                .thenReturn(Lists.newArrayList());


        final List<ErrorReport> errorReports = instance.downloadResource(objectResource, fileStorageHandle);

        InOrder inOrder = Mockito.inOrder(httpFetcher, responseHandlerFactory, objectResource);

        // final List<ErrorReport> firstAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, false);
        inOrder.verify(httpFetcher).execute(any(), any());
        // final List<ErrorReport> secondAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, true);
        inOrder.verify(responseHandlerFactory).getStreamCopyingResponseHandler(any(), any());
        inOrder.verify(httpFetcher).execute(
                argThat(allOf(
                        hasProperty("host", is("example.com")),
                        hasProperty("file", is("/path/" + TRANSFORMED_ENC_FILE_2))
                )),
                argThat(is(responseHandler))
        );
        // if (secondAttemptErrors.isEmpty()) {
        // ..  writeChecksum(objectResource, checksumOut);
        inOrder.verify(objectResource).setChecksum(argThat(is(instanceOf(String.class))));
        inOrder.verify(objectResource).setChecksumType(argThat(is("MD5")));
        // ..  return Lists.newArrayList();
        assertThat(errorReports.isEmpty(), is(true));
        // }
    }

    @Test
    public void itShouldReturnAllTheErrorReportsOfBothFailedAttempts() throws IOException {
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final ObjectResource objectResource = getObjectResource(FULL_URL);
        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final GetRecordResourceOperations instance = new GetRecordResourceOperations(httpFetcher, responseHandlerFactory);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        when(responseHandlerFactory.getStreamCopyingResponseHandler(any(), any()))
                .thenReturn(responseHandler);
        final ErrorReport report1 = mock(ErrorReport.class);
        final ErrorReport report2 = mock(ErrorReport.class);
        when(responseHandler.getExceptions())
                .thenReturn(Lists.newArrayList(report1))
                .thenReturn(Lists.newArrayList(report2));

        final List<ErrorReport> errorReports = instance.downloadResource(objectResource, fileStorageHandle);

        assertThat(errorReports.size(), is(2));
        assertThat(errorReports, containsInAnyOrder(report1, report2));
    }

    private ObjectResource getObjectResource(String url) {
        final ObjectResource objectResource = mock(ObjectResource.class);
        when(objectResource.getXlinkHref()).thenReturn(url);
        return objectResource;
    }


}