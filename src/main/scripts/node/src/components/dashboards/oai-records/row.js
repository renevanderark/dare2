import React from "react";

import { Link } from "react-router";
import { urls } from "../../../router";

class OaiRecordRow extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.identifier !== nextProps.identifier ||
            this.props.activeRecordIdentifier !== nextProps.activeRecordIdentifier ||
            this.props.repositoryName !== nextProps.repositoryName ||
            this.props.dateStamp !== nextProps.dateStamp ||
            this.props.processStatus !== nextProps.processStatus;
    }

    render() {
        const {
            identifier,
            activeRecordIdentifier,
            repositoryName,
            dateStamp,
            processStatus
        } = this.props;

        return (
            <li className={`list-group-item row ${activeRecordIdentifier === identifier ? "active" : ""}`}>

                <div className="col-md-16">
                    <Link to={urls.record(encodeURIComponent(identifier))}>
                        {identifier}
                    </Link>
                </div>
                <div className="col-md-8">
                    <Link to={urls.record(encodeURIComponent(identifier))}>
                        {repositoryName}
                    </Link>
                </div>
                <div className="col-md-5">
                    {dateStamp}
                </div>
                <div className="col-md-2">
                    {processStatus}
                </div>
            </li>
        );
    }
}

OaiRecordRow.propTypes = {
    identifier: React.PropTypes.string.isRequired,
    activeRecordIdentifier: React.PropTypes.string,
    repositoryName: React.PropTypes.string.isRequired,
    dateStamp: React.PropTypes.string.isRequired,
    processStatus: React.PropTypes.string.isRequired
};

export default OaiRecordRow;