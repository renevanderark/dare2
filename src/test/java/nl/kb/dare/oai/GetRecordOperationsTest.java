package nl.kb.dare.oai;

import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.xslt.XsltTransformer;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetRecordOperationsTest {

    @Test
    public void getFileStorageHandleShouldReturnAHandleIfAvailable() throws IOException {
        final FileStorage fileStorage = mock(FileStorage.class);
        final FileStorageHandle handle = mock(FileStorageHandle.class);
        final GetRecordOperations instance = new GetRecordOperations(fileStorage, mock(HttpFetcher.class), mock(ResponseHandlerFactory.class), mock(XsltTransformer.class), (errorReport) -> {});
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        when(fileStorage.create(oaiRecord)).thenReturn(handle);

        final Optional<FileStorageHandle> result = instance.getFileStorageHandle(oaiRecord);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(handle));
    }

    @Test
    public void getFileStorageHandleShouldEmptyOptionalWhenIOExceptionIsCaught() throws IOException {
        final FileStorage fileStorage = mock(FileStorage.class);
        final FileStorageHandle handle = mock(FileStorageHandle.class);
        final GetRecordOperations instance = new GetRecordOperations(fileStorage, mock(HttpFetcher.class), mock(ResponseHandlerFactory.class), mock(XsltTransformer.class), (errorReport) -> {});
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        when(fileStorage.create(oaiRecord)).thenThrow(IOException.class);

        final Optional<FileStorageHandle> result = instance.getFileStorageHandle(oaiRecord);

        assertThat(result.isPresent(), is(false));
    }


}