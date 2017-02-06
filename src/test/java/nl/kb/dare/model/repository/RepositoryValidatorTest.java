package nl.kb.dare.model.repository;


import nl.kb.dare.http.HttpFetcher;
import nl.kb.dare.http.HttpResponseHandler;
import nl.kb.dare.http.responsehandlers.ResponseHandlerFactory;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RepositoryValidatorTest {

    private InputStream mdFormatsXml;
    private InputStream listSetsXml;
    private InputStream corruptXml;

    @Before
    public void setup() {
        mdFormatsXml = RepositoryValidatorTest.class.getResourceAsStream("/model/repository/ListMetadataFormats.xml");
        listSetsXml = RepositoryValidatorTest.class.getResourceAsStream("/model/repository/ListSets.xml");
        corruptXml = new ByteArrayInputStream("<invalid></".getBytes(StandardCharsets.UTF_8));
    }

    private HttpFetcher getMockHttpFetcher(InputStream firstExpected, InputStream secondExpected) {
        return new HttpFetcher() {
            private int count = 0;

            @Override
            public void execute(URL url, HttpResponseHandler responseHandler) {
                if (count == 0) {
                    responseHandler.onResponseData(Response.Status.ACCEPTED, firstExpected);
                } else {
                    responseHandler.onResponseData(Response.Status.ACCEPTED, secondExpected);
                }
                count++;
            }
        };
    }

    @Test
    public void validateShouldSucceedWhetherRepositoryConfigIsSupportedByEndpoint() throws Exception {
        final HttpFetcher mockHttpFetcher = getMockHttpFetcher(listSetsXml, mdFormatsXml);
        final RepositoryValidator instance = new RepositoryValidator(mockHttpFetcher, new ResponseHandlerFactory());
        final Repository validConfig = new Repository("http://example.com", "nl_didl_norm", "uvt:withfulltext:yes", null);

        final RepositoryValidator.ValidationResult validationResult = instance.validate(validConfig);

        assertThat(validationResult.metadataFormatSupported, is(true));
        assertThat(validationResult.setExists, is(true));
    }

    @Test
    public void validateShouldFailWhetherRepositoryConfigIsSupportedByEndpoint() throws Exception {
        final HttpFetcher mockHttpFetcher = getMockHttpFetcher(listSetsXml, mdFormatsXml);
        final RepositoryValidator instance = new RepositoryValidator(mockHttpFetcher, new ResponseHandlerFactory());
        final Repository validConfig = new Repository("http://example.com", "unsupported_Md", "nonexistent_set", null);

        final RepositoryValidator.ValidationResult validationResult = instance.validate(validConfig);

        assertThat(validationResult.metadataFormatSupported, is(false));
        assertThat(validationResult.setExists, is(false));
    }

    @Test(expected = SAXException.class)
    public void validateShouldThrowWhenXmlParsingFails() throws IOException, SAXException {
        final HttpFetcher mockHttpFetcher = getMockHttpFetcher(corruptXml, mdFormatsXml);
        final RepositoryValidator instance = new RepositoryValidator(mockHttpFetcher, new ResponseHandlerFactory());
        final Repository validConfig = new Repository("http://example.com", "nl_didl_norm", "uvt:withfulltext:yes", null);

        instance.validate(validConfig);
    }

    @Test(expected = IOException.class)
    public void validateShouldThrowWhenHttpRequestFails() throws IOException, SAXException {
        final HttpFetcher failingFetcher = (url, responseHandler) -> responseHandler.onRequestError(new Exception("fails"));
        final Repository validConfig = new Repository("http://example.com", "nl_didl_norm", "uvt:withfulltext:yes", null);
        final RepositoryValidator instance = new RepositoryValidator(failingFetcher, new ResponseHandlerFactory());

        instance.validate(validConfig);
    }

    @After
    public void tearDown() {
        try {
            mdFormatsXml.close();
            listSetsXml.close();
        } catch (IOException ignored) {

        }
    }
}