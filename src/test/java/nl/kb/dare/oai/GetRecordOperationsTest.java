package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.files.FileStorageHandle;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.junit.Test;
import org.mockito.InOrder;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class GetRecordOperationsTest {

    @Test
    public void getFileStorageHandleShouldReturnAHandleIfAvailable() throws IOException {
        final FileStorage fileStorage = mock(FileStorage.class);
        final FileStorageHandle handle = mock(FileStorageHandle.class);
        final GetRecordOperations instance = new GetRecordOperations(fileStorage, mock(HttpFetcher.class),
                mock(ResponseHandlerFactory.class), mock(XsltTransformer.class),
                mock(Repository.class),
                mock(GetRecordResourceOperations.class), (errorReport) -> {});
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        when(fileStorage.create(oaiRecord)).thenReturn(handle);

        final Optional<FileStorageHandle> result = instance.getFileStorageHandle(oaiRecord);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(handle));
    }

    @Test
    public void getFileStorageHandleShouldEmptyOptionalWhenIOExceptionIsCaught() throws IOException {
        final FileStorage fileStorage = mock(FileStorage.class);
        final GetRecordOperations instance = new GetRecordOperations(
                fileStorage, mock(HttpFetcher.class), mock(ResponseHandlerFactory.class), mock(XsltTransformer.class),
                mock(Repository.class),
                mock(GetRecordResourceOperations.class), (errorReport) -> {});
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        when(fileStorage.create(oaiRecord)).thenThrow(IOException.class);

        final Optional<FileStorageHandle> result = instance.getFileStorageHandle(oaiRecord);

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void collectResourcesShouldReturnTheListOfObjectResourcesInXml() throws FileNotFoundException {
        final GetRecordOperations instance = new GetRecordOperations(
                mock(FileStorage.class), mock(HttpFetcher.class), mock(ResponseHandlerFactory.class), mock(XsltTransformer.class),
                mock(Repository.class),
                mock(GetRecordResourceOperations.class), (errorReport) -> {});
        final FileStorageHandle handle = mock(FileStorageHandle.class);
        when(handle.getFile("metadata.xml")).thenReturn(GetRecordOperationsTest.class.getResourceAsStream("/oai/mets-experimental.xml"));
        
        final List<ObjectResource> objectResources = instance.collectResources(handle);

        assertThat(objectResources.get(0).getXlinkHref(), is("https://openaccess.leidenuniv.nl/bitstream/1887/20432/3/Stellingen%205.pdf"));
        assertThat(objectResources.get(1).getXlinkHref(), is("https://openaccess.leidenuniv.nl/bitstream/1887/20432/4/back.pdf"));
        assertThat(objectResources.get(2).getXlinkHref(), is("https://openaccess.leidenuniv.nl/bitstream/1887/20432/5/samenvatting.pdf"));
        assertThat(objectResources.get(0).getId(), is("FILE_0001"));
        assertThat(objectResources.get(1).getId(), is("FILE_0002"));
        assertThat(objectResources.get(2).getId(), is("FILE_0003"));
    }

    @Test
    public void collectResourcesShouldLogAnErrorWhenTheListOfResourcesIsEmpty() throws FileNotFoundException {
        final List<ErrorReport> errorReports = Lists.newArrayList();
        final InputStream mets = GetRecordOperationsTest.class.getResourceAsStream("/oai/mets-experimental-no-objects.xml");
        final GetRecordOperations instance = new GetRecordOperations(
                mock(FileStorage.class), mock(HttpFetcher.class), mock(ResponseHandlerFactory.class), mock(XsltTransformer.class),
                mock(Repository.class),
                mock(GetRecordResourceOperations.class), errorReports::add);
        final FileStorageHandle handle = mock(FileStorageHandle.class);
        when(handle.getFile("metadata.xml")).thenReturn(mets);

        final List<ObjectResource> objectResources = instance.collectResources(handle);

        assertThat(objectResources.isEmpty(), is(true));
        assertThat(errorReports.isEmpty(), is(false));
        assertThat(errorReports.get(0), hasProperty("errorStatus", is(ErrorStatus.NO_RESOURCES)));
    }

    @Test
    public void collectResourcesShouldLogAnErrorWhenASaxExceptionIsCaught() throws FileNotFoundException {
        final List<ErrorReport> errorReports = Lists.newArrayList();
        final InputStream badXml = new ByteArrayInputStream("<invalid></".getBytes(StandardCharsets.UTF_8));
        final GetRecordOperations instance = new GetRecordOperations(
                mock(FileStorage.class), mock(HttpFetcher.class), mock(ResponseHandlerFactory.class), mock(XsltTransformer.class),
                mock(Repository.class),
                mock(GetRecordResourceOperations.class), errorReports::add);
        final FileStorageHandle handle = mock(FileStorageHandle.class);
        when(handle.getFile("metadata.xml")).thenReturn(badXml);

        final List<ObjectResource> objectResources = instance.collectResources(handle);

        assertThat(objectResources.isEmpty(), is(true));
        assertThat(errorReports.isEmpty(), is(false));
        assertThat(errorReports.get(0), hasProperty("errorStatus", is(ErrorStatus.XML_PARSING_ERROR)));
    }

    @Test
    public void collectResourcesShouldLogAnErrorWhenAnIOExceptionIsCaught() throws FileNotFoundException {
        final List<ErrorReport> errorReports = Lists.newArrayList();
        final GetRecordOperations instance = new GetRecordOperations(
                mock(FileStorage.class), mock(HttpFetcher.class), mock(ResponseHandlerFactory.class), mock(XsltTransformer.class),
                mock(Repository.class),
                mock(GetRecordResourceOperations.class), errorReports::add);
        final FileStorageHandle handle = mock(FileStorageHandle.class);
        when(handle.getFile("metadata.xml")).thenThrow(IOException.class);

        final List<ObjectResource> objectResources = instance.collectResources(handle);

        assertThat(objectResources.isEmpty(), is(true));
        assertThat(errorReports.isEmpty(), is(false));
        assertThat(errorReports.get(0), hasProperty("errorStatus", is(ErrorStatus.IO_EXCEPTION)));
    }

    @Test
    public void downloadMetdataShouldFetchTheMetadataRecord() {

        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final Repository repository = mock(Repository.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class), httpFetcher,
                responseHandlerFactory, mock(XsltTransformer.class), repository,
                mock(GetRecordResourceOperations.class), (errorReport) -> {});
        when(oaiRecord.getIdentifier()).thenReturn("identifier");
        when(repository.getUrl()).thenReturn("http://example.com");
        when(repository.getMetadataPrefix()).thenReturn("metadataPrefix");

        when(responseHandlerFactory.getXsltTransformingHandler(any(), any())).thenReturn(responseHandler);

        instance.downloadMetadata(mock(FileStorageHandle.class), oaiRecord);

        verify(httpFetcher).execute(argThat(allOf(
                hasProperty("host", is("example.com")),
                hasProperty("query", is("verb=GetRecord&metadataPrefix=metadataPrefix&identifier=identifier"))
        )), argThat(is(responseHandler)));
    }

    @Test
    public void downloadMetadataShouldTransformTheResponseWithTheXsltTransformerStreamingToTheFileStorage() throws IOException {
        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final XsltTransformer xsltTransformer = mock(XsltTransformer.class);
        final OutputStream out = mock(OutputStream.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final Repository repository = mock(Repository.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class),
                httpFetcher, responseHandlerFactory, xsltTransformer, repository,
                mock(GetRecordResourceOperations.class), (errorReport) -> {});

        when(fileStorageHandle.getOutputStream("metadata.xml")).thenReturn(out);
        when(responseHandlerFactory.getXsltTransformingHandler(any(), any())).thenReturn(responseHandler);
        when(oaiRecord.getIdentifier()).thenReturn("identifier");
        when(repository.getUrl()).thenReturn("http://example.com");
        when(repository.getMetadataPrefix()).thenReturn("metadataPrefix");

        instance.downloadMetadata(fileStorageHandle, oaiRecord);

        final InOrder inOrder = inOrder(fileStorageHandle, responseHandlerFactory, httpFetcher, fileStorageHandle);
        inOrder.verify(fileStorageHandle).getOutputStream("metadata.xml");
        inOrder.verify(responseHandlerFactory).getXsltTransformingHandler(
                argThat(is(allOf(
                        instanceOf(StreamResult.class),
                        hasProperty("outputStream", is(out))
                ))), argThat(is(xsltTransformer)));

        inOrder.verify(httpFetcher).execute(argThat(is(instanceOf(URL.class))), argThat(is(responseHandler)));
        inOrder.verify(fileStorageHandle).syncFile(out);
    }

    @Test
    public void downloadMetadataShouldLogAnyExceptionsFromTheResponseHandler() {
        final List<ErrorReport> reports = Lists.newArrayList();
        final Consumer<ErrorReport> onError = reports::add;
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final ErrorReport errorReport = new ErrorReport(new IOException("test"), ErrorStatus.XML_PARSING_ERROR);
        final ErrorReport errorReport2 = new ErrorReport(new IOException("test 2"), ErrorStatus.IO_EXCEPTION);
        final List<ErrorReport> returnedReports = Lists.newArrayList(errorReport, errorReport2);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final Repository repository = mock(Repository.class);
        final GetRecordOperations instance = new GetRecordOperations(
                mock(FileStorage.class), mock(HttpFetcher.class), responseHandlerFactory, mock(XsltTransformer.class),
                repository,
                mock(GetRecordResourceOperations.class), onError);

        when(responseHandlerFactory.getXsltTransformingHandler(any(), any())).thenReturn(responseHandler);
        when(responseHandler.getExceptions()).thenReturn(returnedReports);
        when(oaiRecord.getIdentifier()).thenReturn("identifier");
        when(repository.getUrl()).thenReturn("http://example.com");
        when(repository.getMetadataPrefix()).thenReturn("metadataPrefix");

        instance.downloadMetadata(mock(FileStorageHandle.class), oaiRecord);

        assertThat(reports, containsInAnyOrder(
                allOf(
                    hasProperty("exception", is(instanceOf(IOException.class))),
                    hasProperty("errorStatus", is(ErrorStatus.XML_PARSING_ERROR))
                ), allOf(
                    hasProperty("exception", is(instanceOf(IOException.class))),
                    hasProperty("errorStatus", is(ErrorStatus.IO_EXCEPTION))
                )
        ));
    }

    @Test
    public void downloadMetadataShouldReturnTrueWhenThereWereNoExceptionsFromTheResponseHandler() {
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final Repository repository = mock(Repository.class);
        final GetRecordOperations instance = new GetRecordOperations(
                mock(FileStorage.class), mock(HttpFetcher.class), responseHandlerFactory, mock(XsltTransformer.class),
                repository,
                mock(GetRecordResourceOperations.class), (errorReport) -> {});

        when(responseHandlerFactory.getXsltTransformingHandler(any(), any())).thenReturn(responseHandler);
        when(oaiRecord.getIdentifier()).thenReturn("identifier");
        when(repository.getUrl()).thenReturn("http://example.com");
        when(repository.getMetadataPrefix()).thenReturn("metadataPrefix");

        final boolean result = instance.downloadMetadata(mock(FileStorageHandle.class), oaiRecord);

        assertThat(result, is(true));
    }

    @Test
    public void downloadMetadataShouldReturnFalseWhenThereWereExceptionsFromTheResponseHandler() {
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final Repository repository = mock(Repository.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class), mock(HttpFetcher.class),
                responseHandlerFactory, mock(XsltTransformer.class), repository,
                mock(GetRecordResourceOperations.class), (errorReport) -> {});

        when(responseHandler.getExceptions()).thenReturn(Lists.newArrayList(mock(ErrorReport.class)));
        when(responseHandlerFactory.getXsltTransformingHandler(any(), any())).thenReturn(responseHandler);
        when(oaiRecord.getIdentifier()).thenReturn("identifier");
        when(repository.getUrl()).thenReturn("http://example.com");
        when(repository.getMetadataPrefix()).thenReturn("metadataPrefix");

        final boolean result = instance.downloadMetadata(mock(FileStorageHandle.class), oaiRecord);

        assertThat(result, is(false));
    }

    @Test
    public void downloadMetdataShouldLogAnyCaughtIOExceptionAndThenReturnFalse() {
        final List<ErrorReport> reports = Lists.newArrayList();
        final Consumer<ErrorReport> onError = reports::add;
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final Repository repository = mock(Repository.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class), mock(HttpFetcher.class),
                responseHandlerFactory, mock(XsltTransformer.class), repository, mock(GetRecordResourceOperations.class), onError);

        when(responseHandler.getExceptions()).thenReturn(Lists.newArrayList(mock(ErrorReport.class)));
        when(responseHandlerFactory.getXsltTransformingHandler(any(), any())).thenReturn(responseHandler);

        final boolean result = instance.downloadMetadata(mock(FileStorageHandle.class), oaiRecord);

        assertThat(result, is(false));
        assertThat(reports.get(0), hasProperty("exception", is(instanceOf(IOException.class))));
    }

    @Test
    public void downloadResourcesShouldDownloadAllObjectResourcesAndReturnTrueUponSuccess() throws IOException {
        final GetRecordResourceOperations resourceOperations = mock(GetRecordResourceOperations.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class), mock(HttpFetcher.class),
                mock(ResponseHandlerFactory.class), mock(XsltTransformer.class), mock(Repository.class),
                resourceOperations,
                errorReport -> {});
        final ObjectResource objectResource1 = new ObjectResource();
        final ObjectResource objectResource2 = new ObjectResource();
        final List<ObjectResource> objectResources = Lists.newArrayList(
                objectResource1, objectResource2
        );
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        when(resourceOperations.downloadResource(any(), any())).thenReturn(Lists.newArrayList());

        final boolean success = instance.downloadResources(fileStorageHandle, objectResources);

        verify(resourceOperations).downloadResource(objectResource1, fileStorageHandle);
        verify(resourceOperations).downloadResource(objectResource2, fileStorageHandle);
        assertThat(success, is(true));
    }

    @Test
    public void downloadResourcesShouldReturnFalseUponAnyError() throws IOException {
        final GetRecordResourceOperations resourceOperations = mock(GetRecordResourceOperations.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class), mock(HttpFetcher.class),
                mock(ResponseHandlerFactory.class), mock(XsltTransformer.class), mock(Repository.class),
                resourceOperations,
                errorReport -> {});
        final ObjectResource objectResource1 = new ObjectResource();
        final ObjectResource objectResource2 = new ObjectResource();
        final List<ObjectResource> objectResources = Lists.newArrayList(
                objectResource1, objectResource2
        );
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        when(resourceOperations.downloadResource(any(), any())).thenReturn(Lists.newArrayList(mock(ErrorReport.class)));

        final boolean success = instance.downloadResources(fileStorageHandle, objectResources);

        assertThat(success, is(false));
    }

    @Test
    public void downloadResourcesShouldLogAllDownloadErrors() throws IOException {
        final List<ErrorReport> reports = Lists.newArrayList();
        final Consumer<ErrorReport> onError = reports::add;
        final GetRecordResourceOperations resourceOperations = mock(GetRecordResourceOperations.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class), mock(HttpFetcher.class),
                mock(ResponseHandlerFactory.class), mock(XsltTransformer.class), mock(Repository.class),
                resourceOperations,
                onError);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ErrorReport report1 = mock(ErrorReport.class);
        final ErrorReport report2 = mock(ErrorReport.class);
        final List<ErrorReport> reportedErrors = Lists.newArrayList(
                report1, report2
        );
        when(resourceOperations.downloadResource(any(), any())).thenReturn(reportedErrors);

        instance.downloadResources(fileStorageHandle, Lists.newArrayList(new ObjectResource(), new ObjectResource()));

        assertThat(reports.size(), is(4));
        assertThat(reports.get(0), is(report1));
        assertThat(reports.get(1), is(report2));
        assertThat(reports.get(2), is(report1));
        assertThat(reports.get(3), is(report2));
    }

    @Test
    public void downloadResourcesShouldReturnFalseAndLogAnyCaughtIOException() throws IOException {
        final List<ErrorReport> reports = Lists.newArrayList();
        final Consumer<ErrorReport> onError = reports::add;
        final GetRecordResourceOperations resourceOperations = mock(GetRecordResourceOperations.class);
        final GetRecordOperations instance = new GetRecordOperations(mock(FileStorage.class), mock(HttpFetcher.class),
                mock(ResponseHandlerFactory.class), mock(XsltTransformer.class), mock(Repository.class),
                resourceOperations,
                onError);

        when(resourceOperations.downloadResource(any(), any())).thenThrow(IOException.class);

        final boolean success = instance.downloadResources(mock(FileStorageHandle.class), Lists.newArrayList(new ObjectResource()));


        assertThat(success, is(false));
        assertThat(reports.size(), is(1));
        assertThat(reports.get(0), allOf(
                hasProperty("exception", is(instanceOf(IOException.class))),
                hasProperty("errorStatus", is(ErrorStatus.IO_EXCEPTION))
        ));

    }
}