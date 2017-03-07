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
            onFetchOaiRecord,
            onTestRecord,
            onResetRecord
        } = this.props;

        const resetButton = processStatus !== "PENDING"
            ? (<button className="btn btn-default pull-right" onClick={() => onResetRecord(identifier)}>
                Reset record to pending</button>)
            : null;

        const testButton = testResultsPending
            ? (<button className="btn btn-default pull-right" disabled={true}>Test is running</button>)
            : (<button className="btn btn-default pull-right" onClick={() => onTestRecord(identifier)}>
                Test download of this record</button>);

        return (
            <div>
                <h4>
                    <span className="glyphicon glyphicon-refresh" style={{cursor: "pointer"}}
                          onClick={() => onFetchOaiRecord(identifier)}
                    />{" "}
                    Record
                </h4>
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
                </ul>
                <div className="panel-footer">
                    {resetButton}
                    {testButton}
                    <div className="clearfix" />
                </div>
            </div>
        );
    }
}

export default RecordBody;