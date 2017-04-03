package nl.kb.dare.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.apache.commons.io.IOUtils;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.InputStream;
import java.io.PrintWriter;

public class LoadOracleSchemaTask extends Task {
    private final DBI db;

    public LoadOracleSchemaTask(DBI db) {
        super("create-oracle-schema");
        this.db = db;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
        final InputStream resource = LoadOracleSchemaTask.class.getResourceAsStream("/schema/oracle-schema.sql");
        final String schemaSql = IOUtils.toString(resource, "UTF8");

        StringBuilder sb = new StringBuilder();
        for (String line : schemaSql.split("\n")) {
            if (line.trim().length() == 0) {
                final Handle h = db.open();
                final String sql = sb.toString();
                h.update(sql);
                h.close();
                sb.setLength(0);
            } else {
                sb.append(line).append("\n");
            }
        }
    }
}
