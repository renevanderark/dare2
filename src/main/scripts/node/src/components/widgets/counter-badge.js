import React from "react";
import { numberFormat } from "../../util/format-number";

class CounterBadge extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.count !== nextProps.count;
    }

    render() {
        const {onSetRecordQueryFilter, filterKey, filterValue, count} = this.props;
        return (
            <span className="badge pull-right"
                  style={{cursor: "pointer"}}
                  onClick={() => onSetRecordQueryFilter(filterKey, filterValue)}>
            {numberFormat(count || 0)}
        </span>
        );
    }
}

CounterBadge.propTypes = {
    onSetRecordQueryFilter: React.PropTypes.func.isRequired,
    filterKey: React.PropTypes.string.isRequired,
    filterValue: React.PropTypes.string.isRequired,
    count: React.PropTypes.number
};

export default CounterBadge;
