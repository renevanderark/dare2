package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.files.FileStorage;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryNotifier;
import nl.kb.dare.model.repository.RepositoryValidatorTest;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static nl.kb.dare.oai.ScheduledOaiHarvester.RunState.DISABLED;
import static nl.kb.dare.oai.ScheduledOaiHarvester.RunState.WAITING;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class ScheduledOaiHarvesterTest {

    private static final String DELETED_IDENTIFIER = "ru:oai:repository.ubn.ru.nl:2066/162859";
    private static final String UPDATED_IDENTIFIER = "ru:oai:repository.ubn.ru.nl:2066/161841";
    private InputStream withResumptionToken;
    private InputStream withoutResumptionToken;
    private InputStream corruptXml;

    @Before
    public void setup() {
        withResumptionToken = RepositoryValidatorTest.class.getResourceAsStream("/oai/ListIdentifiersWithResumptionToken.xml");
        withoutResumptionToken = RepositoryValidatorTest.class.getResourceAsStream("/oai/ListIdentifiersWithoutResumptionToken.xml");
        corruptXml = new ByteArrayInputStream("<invalid></".getBytes(StandardCharsets.UTF_8));
    }


    @After
    public void tearDown() {
        try {
            withResumptionToken.close();
            withoutResumptionToken.close();
            corruptXml.close();
        } catch (IOException ignored) {

        }
    }

    @Test
    public void enableAndStartShouldStartTheHarvesters() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                mock(ErrorReportDao.class),
                mock(OaiRecordDao.class),
                new MockHttpFetcher(withResumptionToken, withoutResumptionToken),
                new ResponseHandlerFactory(),
                mock(FileStorage.class), mock(RepositoryNotifier.class)
        );
        when(repositoryDao.list()).thenReturn(Lists.newArrayList());

        instance.enable();
        instance.runOneIteration();

        verify(repositoryDao).list();
        assertThat(instance.getRunState(), is(WAITING));
    }

    @Test
    public void disableShouldDisableAndInterruptTheHarvesters() {
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                mock(RepositoryDao.class),
                mock(ErrorReportDao.class),
                mock(OaiRecordDao.class),
                new MockHttpFetcher(withResumptionToken, withoutResumptionToken),
                new ResponseHandlerFactory(),
                mock(FileStorage.class), mock(RepositoryNotifier.class)
        );

        instance.disable();

        assertThat(instance.getRunState(), is(DISABLED));
    }



    @Test
    public void itShouldHarvestIdentifiersAGivenRepository() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final OaiRecordDao oaiRecordDao = mock(OaiRecordDao.class);
        final RepositoryNotifier repositoryNotifier = mock(RepositoryNotifier.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                mock(ErrorReportDao.class),
                oaiRecordDao,
                new MockHttpFetcher(withResumptionToken, withoutResumptionToken),
                new ResponseHandlerFactory(),
                mock(FileStorage.class), repositoryNotifier
        );
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "set", null, true);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));

        instance.enable();
        instance.runOneIteration();

        verify(repositoryDao).update(repositoryConfig.getId(), repositoryConfig);
        verify(repositoryNotifier).notifyUpdate();
        final ArgumentCaptor<OaiRecord> oaiRecordArgumentCaptor = ArgumentCaptor.forClass(OaiRecord.class);
        verify(oaiRecordDao, times(4)).insert(oaiRecordArgumentCaptor.capture());
        assertThat(oaiRecordArgumentCaptor.getAllValues(), containsInAnyOrder(
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/162830")),
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/162526")),
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/161830")),
            hasProperty("identifier", is(UPDATED_IDENTIFIER))
        ));
    }

    @Test
    public void itShouldLogHarvestingErrors() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final ErrorReportDao errorReportDao = mock(ErrorReportDao.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                errorReportDao,
                mock(OaiRecordDao.class),
                new MockHttpFetcher(corruptXml),
                new ResponseHandlerFactory(),
                mock(FileStorage.class), mock(RepositoryNotifier.class)
        );
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "set", null, true, 123);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));

        instance.enable();
        instance.runOneIteration();

        verify(errorReportDao).insertHarvesterError(
                argThat(allOf(
                        hasProperty("message", is(notNullValue())),
                        hasProperty("filteredStackTrace", is(notNullValue())),
                        hasProperty("dateStamp", is(notNullValue())),
                        hasProperty("repositoryId", is(123))
                ))
        );

    }

    @Test
    public void itShouldLogAnErrorForARecordThatIsUpdatedByTheDataProviderWhenTheExistingRecordIsAlreadyProcessed() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final ErrorReportDao errorReportDao = mock(ErrorReportDao.class);
        final OaiRecordDao oaiRecordDao = mock(OaiRecordDao.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                errorReportDao,
                oaiRecordDao,
                new MockHttpFetcher(withoutResumptionToken),
                new ResponseHandlerFactory(),
                mock(FileStorage.class), mock(RepositoryNotifier.class)
        );
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "set", null, true, 123);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));
        final String duplicateIdentifier = UPDATED_IDENTIFIER;
        final OaiRecord existingRecord = new OaiRecord(duplicateIdentifier, "2017-01-18T01:00:32Z", OaiStatus.AVAILABLE, 123, ProcessStatus.PROCESSED);
        when(oaiRecordDao.findByIdentifier(duplicateIdentifier))
                .thenReturn(existingRecord);

        instance.enable();
        instance.runOneIteration();

        verify(errorReportDao).insertOaiRecordError(argThat(allOf(
                hasProperty("message", is(ErrorStatus.UPDATED_AFTER_PROCESSING.getStatus())),
                hasProperty("errorStatus", is(ErrorStatus.UPDATED_AFTER_PROCESSING)),
                hasProperty("recordIdentifier", is(UPDATED_IDENTIFIER))
        )));
        verify(oaiRecordDao).update(argThat(allOf(
                hasProperty("identifier", is(duplicateIdentifier)),
                hasProperty("processStatus", is(ProcessStatus.UPDATED_AFTER_PROCESSING)),
                hasProperty("oaiStatus", is(OaiStatus.AVAILABLE)),
                hasProperty("dateStamp", is("2017-01-18T01:00:32Z"))
        )));
    }

    @Test
    public void itShouldLogAnErrorForARecordThatIsDeletedByTheDataProviderWhenTheExistingRecordIsAlreadyProcessed() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final ErrorReportDao errorReportDao = mock(ErrorReportDao.class);
        final OaiRecordDao oaiRecordDao = mock(OaiRecordDao.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                errorReportDao,
                oaiRecordDao,
                new MockHttpFetcher(withResumptionToken, withoutResumptionToken),
                new ResponseHandlerFactory(),
                mock(FileStorage.class), mock(RepositoryNotifier.class)
        );
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "set", null, true, 123);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));
        final String duplicateIdentifier = DELETED_IDENTIFIER;
        final OaiRecord existingRecord = new OaiRecord(duplicateIdentifier, "2017-01-18T01:00:32Z", OaiStatus.AVAILABLE, 123, ProcessStatus.PROCESSED);
        when(oaiRecordDao.findByIdentifier(duplicateIdentifier))
                .thenReturn(existingRecord);

        instance.enable();
        instance.runOneIteration();

        verify(errorReportDao).insertOaiRecordError(argThat(allOf(
                hasProperty("message", is(ErrorStatus.DELETED_AFTER_PROCESSING.getStatus())),
                hasProperty("errorStatus", is(ErrorStatus.DELETED_AFTER_PROCESSING)),
                hasProperty("recordIdentifier", is(DELETED_IDENTIFIER))
        )));
        verify(oaiRecordDao).update(argThat(allOf(
                hasProperty("identifier", is(duplicateIdentifier)),
                hasProperty("processStatus", is(ProcessStatus.DELETED_AFTER_PROCESSING)),
                hasProperty("oaiStatus", is(OaiStatus.DELETED)),
                hasProperty("dateStamp", is("2017-01-18T01:00:32Z"))
        )));
    }

    // TODO: specify logic for updated or deleted files which were already processed.
}