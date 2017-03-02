package nl.kb.dare.model.oai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.OaiStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.Update;
import org.skife.jdbi.v2.util.LongMapper;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OaiRecordQuery {
    private static final String UPDATE_SELECTION_SQL =
            "update oai_records %s where identifier in (select identifier from (%s) as intermediary_alias)";
    private Integer repositoryId;
    private Integer offset;
    private Integer limit;
    private ProcessStatus processStatus;
    private OaiStatus oaiStatus;
    private ErrorStatus errorStatus;

    public OaiRecordQuery() {

    }

    OaiRecordQuery(Integer repositoryId, Integer offset, Integer limit, ProcessStatus processStatus,
                   OaiStatus oaiStatus, ErrorStatus errorStatus) {

        this.repositoryId = repositoryId;
        this.offset = offset;
        this.limit = limit;
        this.processStatus = processStatus;
        this.oaiStatus = oaiStatus;
        this.errorStatus = errorStatus;
    }

    @JsonProperty
    public Integer getRepositoryId() {
        return repositoryId;
    }

    @JsonProperty
    public Integer getOffset() {
        return offset;
    }

    @JsonProperty
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty
    public String getProcessStatus() {
        return processStatus != null ? processStatus.getStatus() : "<< no supported parameter defined >>";
    }

    @JsonProperty
    public String getOaiStatus() {
        return oaiStatus != null ? oaiStatus.getStatus() : "<< no supported parameter defined >>";
    }

    @JsonProperty
    public void setProcessStatus(String processStatus) {
        this.processStatus = ProcessStatus.forString(processStatus);
    }

    @JsonProperty
    public void setOaiStatus(String oaiStatus) {
        this.oaiStatus = OaiStatus.forString(oaiStatus);
    }

    @JsonProperty
    public void setErrorStatus(String errorStatus) {
        this.errorStatus = ErrorStatus.forString(errorStatus);
    }

    @JsonProperty
    public String getErrorStatus() {
        return errorStatus != null
                ? errorStatus.getCode() + " - " + errorStatus.getStatus()
                : "<< no supported parameter defined >>";
    }

    @JsonIgnore
    public List<OaiRecord> getResults(DBI dbi) {
        try (final Handle h = dbi.open()) {
            return getBaseFilter(h, "select distinct oai_records.* from oai_records", null)
                    .withLimit(limit)
                    .withOffset(offset)
                    .build()
                    .getResults();
        }
    }


    @JsonIgnore
    public Long getCount(DBI dbi) {
        try (final Handle h = dbi.open()) {
            return getBaseFilter(h, "select count(distinct(identifier)) from oai_records",null)
                    .build()
                    .getCount();
        }
    }

    public void resetToPending(DBI dbi) {
        try (final Handle h = dbi.open()) {
            getBaseFilter(h,
                    "select identifier from oai_records",
                    "set oai_records.process_status_code = " + ProcessStatus.PENDING.getCode())
                .build()
                .executeUpdate();
        }
    }

    private Builder getBaseFilter(Handle h, String selectClause, String updateClause) {
        return new Builder(h, selectClause, updateClause)
                .withFilter("repository_id", repositoryId)
                .withFilter("process_status_code", processStatus)
                .withFilter("oai_status_code", oaiStatus)
                .withErrorFilter(errorStatus);
    }



    private class Builder {
        private final Map<String, Object> filters = Maps.newLinkedHashMap();
        private final String selectClause;
        private final String updateClause;
        private final Handle h;
        private Integer limit = null;
        private Integer offset = null;
        private boolean withErrorSelect = false;
        private Integer withErrorStatus = null;

        private Query<Map<String, Object>> query;
        private Update update;

        private Builder(Handle h, String selectClause, String updateClause) {
            this.h = h;
            this.selectClause = selectClause;
            this.updateClause = updateClause;
        }

        Builder withFilter(String field, Object filterValue) {
            if (filterValue != null) {
                if (filterValue instanceof ProcessStatus) {
                    filters.put(field, ((ProcessStatus) filterValue).getCode());
                } else if (filterValue instanceof OaiStatus) {
                    filters.put(field, ((OaiStatus) filterValue).getCode());
                } else {
                    filters.put(field, filterValue);
                }
            }
            return this;
        }

        Builder withLimit(Integer limit) {
            if (limit != null) {
                this.limit = limit;
            }
            return this;
        }

        Builder withOffset(Integer offset) {
            if (offset != null) {
                this.offset = offset;
            }
            return this;
        }

        Builder build() {
            final StringBuilder sb = new StringBuilder(selectClause);

            if (withErrorSelect) {
                sb.append(", oai_record_errors");
            }

            if (filters.size() > 0 || withErrorSelect) {
                sb.append(" where ");
            }


            final List<String> clauses = filters.keySet()
                    .stream()
                    .map(field -> String.format("%s = :%s", field, field))
                    .collect(toList());

            if (withErrorSelect) {
                clauses.add("oai_record_errors.record_identifier = oai_records.identifier");
                clauses.add("oai_record_errors.status_code = :error_status_code");
            }

            sb.append(clauses.stream().collect(joining(" and ")));

            if (this.limit != null) {
                sb.append(" limit :limit");
            }
            if (this.offset != null) {
                sb.append(" offset :offset");
            }



            if (updateClause == null) {
                query = h.createQuery(sb.toString());
            } else {
                final String updateSql = String.format(UPDATE_SELECTION_SQL, updateClause, sb.toString());

                update = h.createStatement(updateSql);

            }

            final SQLStatement statement = updateClause == null ? query : update;

            filters.keySet().forEach(key -> statement.bind(key, filters.get(key)));

            if (withErrorSelect) {
                statement.bind("error_status_code", withErrorStatus);
            }

            if (this.limit != null) {
                statement.bind("limit", limit);
            }
            if (this.offset != null) {
                statement.bind("offset", offset);
            }

            return this;
        }

        Builder withErrorFilter(ErrorStatus errorStatus) {
            if (errorStatus == null) {
                return this;
            }
            withErrorSelect = true;
            withErrorStatus = errorStatus.getCode();
            return this;
        }


        List<OaiRecord> getResults() {
            return query.map(new OaiRecordMapper()).list();
        }

        Long getCount() {
            return query.map(LongMapper.FIRST).first();
        }

        void executeUpdate() {
            update.execute();
        }

    }
}
