import React from "react";

class RecordBody extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.identifier !== nextProps.identifier ||
            this.props.processStatus !== nextProps.processStatus ||
            this.props.dateStamp !== nextProps.dateStamp ||
            this.props.repositoryName !== nextProps.repositoryName ||
            this.props.updateCount !== nextProps.updateCount ||
            this.props.testResultsPending !== nextProps.testResultsPending;
    }
    
    render() {
        const {
            identifier,
            processStatus,
            dateStamp,
            repositoryName,
            updateCount,
            testResultsPending
        } = this.props;

        const {
            onTestRecord
        } = this.props;

        return (
            <div>
                <h3>Record</h3>
                <ul className="list-group">
                    <li className="row list-group-item">
                        <strong className="col-md-4">Identifier</strong>
                        <span className="col-md-27">{identifier}</span>
                        <span className="col-md-1">
                            {processStatus !== "PENDING" && processStatus !== "PROCESSING"
                                ? (<a type="download" className="pull-right"
                                      href={`/records/${encodeURIComponent(identifier)}/download`}>
                                    <span className="glyphicon glyphicon-download-alt" />
                                </a>)
                                : null}
                        </span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Datestamp</strong>
                        <span className="col-md-16">{dateStamp}</span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Processing status</strong>
                        <span className="col-md-16">{processStatus}</span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Data provider</strong>
                        <span className="col-md-16">{repositoryName}</span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Update count</strong>
                        <span className="col-md-16">{updateCount}</span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Test record</strong>
                        <span className="col-md-16">
                            {testResultsPending
                                ? (<span>Test is running</span>)
                                : (<a onClick={() => onTestRecord(identifier)}>Start test</a>)
                            }
                        </span>
                    </li>
                </ul>
            </div>
        );
    }
}

export default RecordBody;