import React from "react";
import {numberFormat} from "../../../util/format-number";

class ResultHeader extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.count !== nextProps.count;
    }

    render() {
        const { onRefetchRecords, count } = this.props;

        return (
            <h4 style={{marginTop: "20px"}}>
                    <span className="glyphicon glyphicon-refresh" style={{cursor: "pointer"}}
                          onClick={() => onRefetchRecords()}
                    />
                {" "}
                Results ({numberFormat(count)})
            </h4>
        );
    }
}
ResultHeader.propTypes = {
    count: React.PropTypes.number.isRequired,
    onRefetchRecords: React.PropTypes.func.isRequired
};

export default ResultHeader;