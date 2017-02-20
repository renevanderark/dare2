import React from "react";
import { numberFormat } from "../../util/format-number";

const CounterBadge = ({ onSetRecordQueryFilter, filterKey, filterValue, count }) => (
    <span className="badge pull-right"
          style={{cursor: "pointer"}}
          onClick={() => onSetRecordQueryFilter(filterKey, filterValue)}>
        {numberFormat(count || 0)}
    </span>
);

CounterBadge.propTypes = {
    onSetRecordQueryFilter: React.PropTypes.func.isRequired,
    filterKey: React.PropTypes.string.isRequired,
    filterValue: React.PropTypes.string.isRequired,
    count: React.PropTypes.number
};

export default CounterBadge;
