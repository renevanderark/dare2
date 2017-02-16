package nl.kb.dare.endpoints;

import nl.kb.dare.oai.ScheduledOaiHarvester;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OaiHarvesterEndpointTest {

    @Test
    public void startShouldEnableAndStartTheHarvesterService() throws Exception {
        final ScheduledOaiHarvester harvester = mock(ScheduledOaiHarvester.class);
        final OaiHarvesterEndpoint instance = new OaiHarvesterEndpoint(harvester);

        final Response response = instance.start();

        verify(harvester).enableAndStart();
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void startShouldReturnInternalServerErrorWhenHarvesterThrows() throws Exception {
        final ScheduledOaiHarvester harvester = mock(ScheduledOaiHarvester.class);
        final OaiHarvesterEndpoint instance = new OaiHarvesterEndpoint(harvester);
        doThrow(Exception.class).when(harvester).enableAndStart();

        final Response response = instance.start();

        verify(harvester).enableAndStart();
        assertThat(response.getStatus(), is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    public void disableShouldDisableTheHarvesterService() {
        final ScheduledOaiHarvester harvester = mock(ScheduledOaiHarvester.class);
        final OaiHarvesterEndpoint instance = new OaiHarvesterEndpoint(harvester);

        final Response response = instance.disable();

        verify(harvester).disable();
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }
}