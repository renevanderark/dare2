package nl.kb.dare;

import nl.kb.dare.model.oai.OaiRecordDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class TmpTest {
    private static final String CREATE_TABLE = "CREATE TABLE oai_records (" +
            "  identifier varchar(128) NOT NULL)";

    private OaiRecordDao oaiRecordDao;
    private Handle handle;

    @Before
    public void setup() {
        final DBI dbi = new DBI("jdbc:oracle:thin:system/oracle@localhost:49161:xe");
        handle = dbi.open();
        handle.execute(CREATE_TABLE);
        oaiRecordDao = dbi.onDemand(OaiRecordDao.class);
    }



    @After
    public void tearDown() {
        handle.close();
    }

    @Test
    public void shouldRun() {
        System.out.println("Hello oracle?");
    }

}
