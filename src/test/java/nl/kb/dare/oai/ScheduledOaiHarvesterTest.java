package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.oai.OaiRecord;
import nl.kb.dare.model.oai.OaiRecordDao;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidatorTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

    private InputStream withResumptionToken;
    private InputStream withoutResumptionToken;
    private InputStream corruptXml;
    private InputStream withDuplicates;

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
    public void itShouldHarvestIdentifiersAGivenRepository() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final OaiRecordDao oaiRecordDao = mock(OaiRecordDao.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                mock(ErrorReportDao.class),
                oaiRecordDao,
                new MockHttpFetcher(withResumptionToken, withoutResumptionToken),
                new ResponseHandlerFactory()
        );
        final Repository repositoryConfig = new Repository("http://example.com", "prefix", "set", null);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));

        instance.runOneIteration();

        final ArgumentCaptor<OaiRecord> oaiRecordArgumentCaptor = ArgumentCaptor.forClass(OaiRecord.class);
        verify(oaiRecordDao, times(5)).insert(oaiRecordArgumentCaptor.capture());
        assertThat(oaiRecordArgumentCaptor.getAllValues(), containsInAnyOrder(
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/162830")),
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/162859")),
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/162526")),
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/161830")),
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/161841"))
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
                new ResponseHandlerFactory()
        );
        final Repository repositoryConfig = new Repository("http://example.com", "prefix", "set", null, 123);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));

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
    public void itShouldLogAnErrorWhenARecordIsUpdatedThatIsAlreadyThere() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final ErrorReportDao errorReportDao = mock(ErrorReportDao.class);
        final OaiRecordDao oaiRecordDao = mock(OaiRecordDao.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                errorReportDao,
                oaiRecordDao,
                new MockHttpFetcher(withoutResumptionToken),
                new ResponseHandlerFactory()
        );
        final Repository repositoryConfig = new Repository("http://example.com", "prefix", "set", null, 123);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));
        final String duplicateIdentifier = "ru:oai:repository.ubn.ru.nl:2066/161841";
        final OaiRecord existingRecord = new OaiRecord(duplicateIdentifier, "2017-01-18T01:00:32Z", "", 123, "pending");
        when(oaiRecordDao.findByIdentifier(duplicateIdentifier))
                .thenReturn(existingRecord);

        instance.runOneIteration();

        verify(errorReportDao).insertOaiRecordError(argThat(
                hasProperty("message", is("record was updated by provider after first encounter"))));
        verify(oaiRecordDao).update(argThat(hasProperty("identifier", is(duplicateIdentifier))));
    }

    @Test
    public void itShouldLogAnErrorWhenARecordIsDeletedThatIsAlreadyThere() throws Exception {
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final ErrorReportDao errorReportDao = mock(ErrorReportDao.class);
        final OaiRecordDao oaiRecordDao = mock(OaiRecordDao.class);
        final ScheduledOaiHarvester instance = new ScheduledOaiHarvester(
                repositoryDao,
                errorReportDao,
                oaiRecordDao,
                new MockHttpFetcher(withResumptionToken, withoutResumptionToken),
                new ResponseHandlerFactory()
        );
        final Repository repositoryConfig = new Repository("http://example.com", "prefix", "set", null, 123);
        when(repositoryDao.list()).thenReturn(Lists.newArrayList(repositoryConfig));
        final String duplicateIdentifier = "ru:oai:repository.ubn.ru.nl:2066/162859";
        final OaiRecord existingRecord = new OaiRecord(duplicateIdentifier, "2017-01-18T01:00:32Z", "", 123, "pending");
        when(oaiRecordDao.findByIdentifier(duplicateIdentifier))
                .thenReturn(existingRecord);

        instance.runOneIteration();

        verify(errorReportDao).insertOaiRecordError(argThat(
                hasProperty("message", is("record was deleted by provider after first encounter"))));
        verify(oaiRecordDao).update(argThat(hasProperty("identifier", is(duplicateIdentifier))));
    }

}