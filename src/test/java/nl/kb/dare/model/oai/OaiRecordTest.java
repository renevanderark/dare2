package nl.kb.dare.model.oai;

import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class OaiRecordTest {

    @Test
    public void equalsShouldFailWhenTheOaiPropertiesAreNotTheSame() {
        assertThat(new OaiRecord("id","date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING),
                not(new OaiRecord("id2", "date", OaiStatus.AVAILABLE,123, ProcessStatus.PENDING)));

        assertThat(new OaiRecord("id","date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING).hashCode(),
                not(new OaiRecord("id2", "date", OaiStatus.AVAILABLE,123, ProcessStatus.PENDING).hashCode()));

        assertThat(new OaiRecord("id", "date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING),
                not(new OaiRecord("id", "date2",OaiStatus.AVAILABLE, 123,ProcessStatus.PENDING)));

        assertThat(new OaiRecord("id", "date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING).hashCode(),
                not(new OaiRecord("id", "date2",OaiStatus.AVAILABLE, 123,ProcessStatus.PENDING).hashCode()));

        assertThat(new OaiRecord("id", "date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING),
                not(new OaiRecord("id", "date", OaiStatus.DELETED, 123, ProcessStatus.PENDING)));

        assertThat(new OaiRecord("id", "date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING).hashCode(),
                not(new OaiRecord("id", "date", OaiStatus.DELETED, 123, ProcessStatus.PENDING).hashCode()));

        assertThat(new OaiRecord("id", "date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING),
                not(new OaiRecord("id", "date", OaiStatus.DELETED, 124, ProcessStatus.PENDING)));

        assertThat(new OaiRecord("id", "date", OaiStatus.AVAILABLE, 123, ProcessStatus.PENDING).hashCode(),
                not(new OaiRecord("id", "date", OaiStatus.DELETED, 124, ProcessStatus.PENDING).hashCode()));

    }

    @Test
    public void equalsShouldSucceedWhenOnlyTheNonOaiPropertiesDiffer() {
        assertThat(new OaiRecord(
                "id",
                "date",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.PENDING
        ), equalTo(new OaiRecord(
                "id",
                "date",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.SKIP
        )));

        assertThat(new OaiRecord(
                "id",
                "date",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.PENDING
        ).hashCode(), equalTo(new OaiRecord(
                "id",
                "date",
                OaiStatus.AVAILABLE,
                123,
                ProcessStatus.SKIP
        ).hashCode()));
    }
}