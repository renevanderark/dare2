package nl.kb.dare.endpoints;

import com.google.common.collect.Lists;
import nl.kb.filestorage.FileStorage;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.oai.OaiRecordQuery;
import nl.kb.dare.model.oai.OaiRecordQueryFactory;
import nl.kb.dare.model.oai.OaiRecordResult;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.OaiRecordErrorReport;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.xslt.XsltTransformer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OaiRecordsEndpointTest {

    private OaiRecordQueryFactory oaiRecordQueryFactory;
    private DBI dbi;
    private ErrorReportDao errorReportDao;
    private OaiRecordsEndpoint instance;
    private OaiRecordDao oaiRecordDao;

    @Before
    public void setup() {
        oaiRecordQueryFactory = mock(OaiRecordQueryFactory.class);
        dbi = mock(DBI.class);
        errorReportDao = mock(ErrorReportDao.class);
        oaiRecordDao = mock(OaiRecordDao.class);
        instance = new OaiRecordsEndpoint(
                dbi,
                oaiRecordDao,
                errorReportDao,
                oaiRecordQueryFactory,
                mock(FileStorage.class),
                mock(RepositoryDao.class),
                mock(HttpFetcher.class),
                mock(ResponseHandlerFactory.class),
                mock(XsltTransformer.class),
                mock(FileStorage.class)
        );
    }

    @Test
    public void indexShouldReturnTheRecords() {
        final OaiRecordQuery oaiRecordQuery = mock(OaiRecordQuery.class);
        final long expectedCount = 123L;
        final List<OaiRecord> expectedResult = Lists.newArrayList();
        when(oaiRecordQueryFactory.getInstance(any(), any(), any(), any(), any(), any()))
                .thenReturn(oaiRecordQuery);
        when(oaiRecordQuery.getCount(dbi)).thenReturn(expectedCount);
        when(oaiRecordQuery.getResults(dbi)).thenReturn(expectedResult);

        final Response response = instance.index(1, 2, 3,
                ProcessStatus.FAILED.getStatus(),
                ErrorStatus.NOT_FOUND.getCode(),
                OaiStatus.AVAILABLE.getStatus());

        verify(oaiRecordQueryFactory).getInstance(1, 2, 3,
                ProcessStatus.FAILED, OaiStatus.AVAILABLE, ErrorStatus.NOT_FOUND);
        verify(oaiRecordQuery).getResults(dbi);
        verify(oaiRecordQuery).getCount(dbi);
        assertThat(response.getEntity(), allOf(
                is(instanceOf(OaiRecordResult.class)),
                hasProperty("oaiRecordQuery", is(oaiRecordQuery)),
                hasProperty("result", is(expectedResult)),
                hasProperty("count", is(expectedCount))
        ));
    }

    @Test
    public void resetShouldSetTheRecordsToPendingAndReturnTheUpdatedRecords() {
        final OaiRecordQuery oaiRecordQuery = mock(OaiRecordQuery.class);
        final long expectedCount = 123L;
        final OaiRecord oaiRecord = new OaiRecord();
        final String expectedOaiRecordIdentifier = "expected-record-id";
        oaiRecord.setIdentifier(expectedOaiRecordIdentifier);
        final List<OaiRecord> expectedResult = Lists.newArrayList();

        when(oaiRecordQueryFactory.getInstance(any(), any(), any(), any(), any(), any()))
                .thenReturn(oaiRecordQuery);
        when(oaiRecordQuery.getCount(dbi)).thenReturn(expectedCount);
        when(oaiRecordQuery.getResults(dbi))
                .thenReturn(Lists.newArrayList(oaiRecord))
                .thenReturn(expectedResult);

        final Response response = instance.bulkReset(1,
                ProcessStatus.FAILED.getStatus(),
                ErrorStatus.NOT_FOUND.getCode(),
                OaiStatus.AVAILABLE.getStatus());


        final InOrder inOrder = inOrder(oaiRecordQueryFactory, oaiRecordQuery, errorReportDao);

        inOrder.verify(oaiRecordQueryFactory).getInstance(1, null, null,
                ProcessStatus.FAILED, OaiStatus.AVAILABLE, ErrorStatus.NOT_FOUND);
        inOrder.verify(oaiRecordQuery).getResults(dbi);
        inOrder.verify(oaiRecordQuery).resetToPending(dbi);
        inOrder.verify(errorReportDao).removeForOaiRecord(expectedOaiRecordIdentifier);
        inOrder.verify(oaiRecordQuery).getResults(dbi);
        inOrder.verify(oaiRecordQuery).getCount(dbi);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getEntity(), allOf(
                is(instanceOf(OaiRecordResult.class)),
                hasProperty("oaiRecordQuery", is(oaiRecordQuery)),
                hasProperty("result", is(expectedResult)),
                hasProperty("count", is(expectedCount))
        ));
    }

    @Test
    public void getShouldReturnTheRecordAndItsErrors() {
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final ArrayList<OaiRecordErrorReport> errorReports = Lists.newArrayList();
        when(oaiRecordDao.findByIdentifier("the-id")).thenReturn(oaiRecord);
        when(errorReportDao.findByRecordIdentifier("the-id")).thenReturn(errorReports);

        final Response response = instance.get("the-id");

        @SuppressWarnings("unchecked")
        final Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertThat(entity.get("record"), is(oaiRecord));
        assertThat(entity.get("errorReports"), is(errorReports));
    }

    @Test
    public void resetShouldResetTheRecordToPendingAndReturnIt() {
        final OaiRecord oaiRecord = mock(OaiRecord.class);
        final ArrayList<OaiRecordErrorReport> errorReports = Lists.newArrayList();
        final String oaiRecordIdentifier = "the-id";
        when(oaiRecord.getIdentifier()).thenReturn(oaiRecordIdentifier);
        when(oaiRecordDao.findByIdentifier(oaiRecordIdentifier)).thenReturn(oaiRecord);
        when(errorReportDao.findByRecordIdentifier(oaiRecordIdentifier)).thenReturn(errorReports);

        final Response response = instance.reset(oaiRecordIdentifier);

        verify(oaiRecord).setProcessStatus(ProcessStatus.PENDING);
        verify(errorReportDao).removeForOaiRecord(oaiRecordIdentifier);
        verify(oaiRecordDao).update(oaiRecord);

        @SuppressWarnings("unchecked")
        final Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertThat(entity.get("record"), is(oaiRecord));
        assertThat(entity.get("errorReports"), is(errorReports));
    }

}