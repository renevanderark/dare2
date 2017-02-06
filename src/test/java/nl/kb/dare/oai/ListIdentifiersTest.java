package nl.kb.dare.oai;

import com.google.common.collect.Lists;
import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryValidatorTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ListIdentifiersTest {


    private InputStream withResumptionToken;
    private InputStream withoutResumptionToken;
    private InputStream withResumptionToken2;
    private InputStream corruptXml;

    private class MockHttpFetcher implements HttpFetcher {

        int count = 0;
        private final InputStream[] mockResponses;

        private MockHttpFetcher(InputStream... mockResponses) {
            this.mockResponses = mockResponses;
        }

        @Override
        public void execute(URL url, HttpResponseHandler responseHandler) {
            responseHandler.onResponseData(Response.Status.ACCEPTED, mockResponses[count++]);
        }

    }



    @Before
    public void setup() {
        withResumptionToken = RepositoryValidatorTest.class.getResourceAsStream("/oai/ListIdentifiersWithResumptionToken.xml");
        withResumptionToken2 = RepositoryValidatorTest.class.getResourceAsStream("/oai/ListIdentifiersWithResumptionToken.xml");
        withoutResumptionToken = RepositoryValidatorTest.class.getResourceAsStream("/oai/ListIdentifiersWithoutResumptionToken.xml");
        corruptXml = new ByteArrayInputStream("<invalid></".getBytes(StandardCharsets.UTF_8));
    }


    @After
    public void tearDown() {
        try {
            withResumptionToken.close();
            withResumptionToken2.close();
            withoutResumptionToken.close();
            corruptXml.close();
        } catch (IOException ignored) {

        }
    }

    @Test
    public void harvestShouldHarvestUntilThereAreNoMoreResumptionTokens() {
        final Repository repositoryConfig = new Repository("http://oai-endpoint.org", "md:pref", "setName", null);
        final MockHttpFetcher httpFetcher = new MockHttpFetcher(withResumptionToken, withoutResumptionToken);
        final Consumer<Repository> repositoryConsumer = (repoDone) -> { };
        final Consumer<Exception> errorConsumer = (err) -> { };
        final ListIdentifiers instance = new ListIdentifiers(repositoryConfig, httpFetcher, new ResponseHandlerFactory(), repositoryConsumer, errorConsumer);

        instance.harvest();

        assertThat(httpFetcher.count, is(2));
    }

    @Test
    public void harvestShouldInvokeOnHarvestCompleteOnceWithRepoSetToLatestDatestampFromLastHarvestResponse() {
        final Repository repositoryConfig = new Repository("http://oai-endpoint.org", "md:pref", "setName", null);
        final MockHttpFetcher httpFetcher = new MockHttpFetcher(withResumptionToken, withResumptionToken2, withoutResumptionToken);
        final List<String> dateStamps = Lists.newArrayList();
        final Consumer<Repository> repositoryConsumer = (repoDone) -> dateStamps.add(repoDone.getDateStamp());
        final Consumer<Exception> errorConsumer = (err) -> { };
        final ListIdentifiers instance = new ListIdentifiers(repositoryConfig, httpFetcher, new ResponseHandlerFactory(), repositoryConsumer, errorConsumer);

        instance.harvest();

        assertThat(dateStamps.size(), is(1));
        // Value taken from last record in ListIdentifiersWithoutResumptionToken.xml
        assertThat(dateStamps.get(0), is("2017-01-18T01:00:40Z"));
    }

    @Test
    public void harvestShouldLogErrorAndTerminateAfterLastSuccesfulResponse() {
        final String orignalDateStamp = "initialDatestampValue";
        final Repository repositoryConfig = new Repository("http://oai-endpoint.org", "md:pref", "setName", orignalDateStamp);
        final MockHttpFetcher httpFetcher = new MockHttpFetcher(corruptXml);
        final List<String> dateStamps = Lists.newArrayList();
        final List<Exception> exceptions = Lists.newArrayList();
        final Consumer<Repository> repositoryConsumer = (repoDone) -> dateStamps.add(repoDone.getDateStamp());
        final Consumer<Exception> errorConsumer = exceptions::add;
        final ListIdentifiers instance = new ListIdentifiers(repositoryConfig, httpFetcher, new ResponseHandlerFactory(), repositoryConsumer, errorConsumer);

        instance.harvest();

        assertThat(exceptions.size(), is(1));
        assertThat(exceptions.get(0), instanceOf(SAXException.class));

        assertThat(dateStamps.size(), is(1));
        // Original value
        assertThat(dateStamps.get(0), is(orignalDateStamp));


    }
}