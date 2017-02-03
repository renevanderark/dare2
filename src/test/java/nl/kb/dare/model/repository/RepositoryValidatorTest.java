package nl.kb.dare.model.repository;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryValidatorTest {

    private InputStream mdFormatsXml;
    private InputStream listSetsXml;


    @Before
    public void setup() {
        mdFormatsXml = RepositoryValidatorTest.class.getResourceAsStream("/model/repository/ListMetadataFormats.xml");
        listSetsXml = RepositoryValidatorTest.class.getResourceAsStream("/model/repository/ListSets.xml");

    }

    @Test
    public void validateShouldSucceedWhetherRepositoryConfigIsSupportedByEndpoint() throws Exception {
        final HttpClient httpClient = mock(HttpClient.class);
        final RepositoryValidator instance = new RepositoryValidator(httpClient);
        final Repository validConfig = new Repository("http://example.com", "nl_didl_norm", "uvt:withfulltext:yes", null);

        final HttpResponse listSetsResponse = mockHttpResponse(listSetsXml);
        final HttpResponse mdFormatsResponse = mockHttpResponse(mdFormatsXml);
        when(httpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(listSetsResponse)
                .thenReturn(mdFormatsResponse);

        final RepositoryValidator.ValidationResult validationResult = instance.validate(validConfig);

        assertThat(validationResult.metadataFormatSupported, is(true));
        assertThat(validationResult.setExists, is(true));
    }

    @Test
    public void validateShouldFailWhetherRepositoryConfigIsSupportedByEndpoint() throws Exception {
        final HttpClient httpClient = mock(HttpClient.class);
        final RepositoryValidator instance = new RepositoryValidator(httpClient);
        final Repository validConfig = new Repository("http://example.com", "unsupported_Md", "nonexistent_set", null);

        final HttpResponse listSetsResponse = mockHttpResponse(listSetsXml);
        final HttpResponse mdFormatsResponse = mockHttpResponse(mdFormatsXml);
        when(httpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(listSetsResponse)
                .thenReturn(mdFormatsResponse);

        final RepositoryValidator.ValidationResult validationResult = instance.validate(validConfig);

        assertThat(validationResult.metadataFormatSupported, is(false));
        assertThat(validationResult.setExists, is(false));
    }

    private HttpResponse mockHttpResponse(InputStream responseStream) throws IOException {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(responseStream);
        return httpResponse;
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