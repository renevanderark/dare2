package nl.kb.dare.model.oai.oracle;

import nl.kb.dare.model.oai.OaiRecordQueryFactory;
import nl.kb.dare.model.oai.OaiRecordStatusAggregator;
import org.skife.jdbi.v2.DBI;

import java.math.BigDecimal;
import java.util.Map;

public class OracleStatusAggregator extends OaiRecordStatusAggregator {
    public OracleStatusAggregator(DBI db, OaiRecordQueryFactory oaiRecordQueryFactory) {
        super(db, oaiRecordQueryFactory);
    }

    protected Integer getRowInt(Map<String, Object> row, String repository_id) {
        return ((BigDecimal) row.get(repository_id)).intValue();
    }
}
