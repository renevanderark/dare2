package nl.kb.dare.oai;

import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GetRecordTest {

    @Test
    public void fetchShouldReturnFailedWhenNoFileStorageHandleCouldBeCreated()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord, false);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.empty());

        final ProcessStatus result = instance.fetch();
        verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenDownloadMetadataFails()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord, false);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(false);

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations, getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenDownloadResourcesFails()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord, false);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(true);
        when(getRecordOperations.downloadResources(any())).thenReturn(false);

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations, getRecordOperations, getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnProcessedWhenAllOperationsSucceed()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord, false);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(true);
        when(getRecordOperations.downloadResources(any())).thenReturn(true);

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations, getRecordOperations, getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.PROCESSED));
    }

    @Test
    public void fetchShouldDeleteFilesAfterProcessingWhenInSampleMode() throws IOException {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord, true);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(true);
        when(getRecordOperations.downloadResources(any())).thenReturn(true);

        instance.fetch();

        verify(fileStorageHandle).deleteFiles();
    }
}