import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";

class OaiRecordDashboard extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            expandedTraces: []
        };
    }

    componentWillReceiveProps(nextProps) {
        const {onFetchOaiRecord} = this.props;

        if (nextProps.identifier !== this.props.identifier) {
            onFetchOaiRecord(nextProps.identifier);
            this.setState({expandedTraces: []});
        }
    }

    componentDidMount() {
        const { oaiRecord : { record }, identifier, onFetchOaiRecord } = this.props;

        if (!record || record.identifier !== identifier) {
            onFetchOaiRecord(identifier);
        }
    }

    toggleStacktrace(idx) {
        const { expandedTraces } = this.state;
        if (expandedTraces.indexOf(idx) < 0) {
            this.setState({expandedTraces: expandedTraces.concat(idx)});
        } else {
            this.setState({expandedTraces: expandedTraces.filter(id => id !== idx)});
        }
    }

    render() {
        const { oaiRecord: { record, collapsed, errorReports, repositoryName }, identifier } = this.props;
        const { onTogglePanelCollapse } = this.props;

        const expandedTraces = this.state.expandedTraces;

        const errorReportListing = errorReports && errorReports.length > 0 ? (
            <div>
                <h3>Error reports</h3>
                <ul className="list-group">
                    {errorReports.map((errorReport, i) => (
                        <li key={i} className="list-group-item">
                            <div className="row">
                                <span className="col-md-8">
                                    {errorReport.errorStatusCode} - {errorReport.errorStatus}
                                </span>
                                <span className="col-24">
                                    <a target="_blank" href={errorReport.url}>
                                        {errorReport.url}
                                    </a>
                                </span>
                            </div>
                            <div className="row">
                                <span className="col-md-32">
                                    {errorReport.message} ({errorReport.dateStamp})
                                    <span style={{cursor: "pointer"}}
                                          onClick={() => this.toggleStacktrace(i) }
                                          className={`glyphicon glyphicon-${expandedTraces.indexOf(i) < 0 ?
                                              "expand" : "collapse-down"} pull-right`} />
                                </span>
                            </div>
                            { expandedTraces.indexOf(i) > -1
                                ? (<pre>{errorReport.filteredStackTrace}</pre>)
                                : null }
                        </li>
                    ))}
                </ul>
            </div>
        ) : null;
        const body = !record
            ? (<div>Loading: {identifier}</div>)
            : (<div>
                <h3>Record</h3>
                <ul className="list-group">
                    <li className="row list-group-item">
                        <strong className="col-md-4">Identifier</strong>
                        <span className="col-md-27">{record.identifier}</span>
                        <span className="col-md-1">
                            {record.processStatus !== "PENDING" && record.processStatus !== "PROCESSING"
                                ? (<a type="download" className="pull-right"
                                    href={`/records/${encodeURIComponent(record.identifier)}/download`}>
                                        <span className="glyphicon glyphicon-download-alt" />
                                    </a>)
                                : null}
                        </span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Datestamp</strong>
                        <span className="col-md-16">{record.dateStamp}</span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Processing status</strong>
                        <span className="col-md-16">{record.processStatus}</span>
                    </li>
                    <li className="row list-group-item">
                        <strong className="col-md-4">Data provider</strong>
                        <span className="col-md-16">{repositoryName}</span>
                    </li>
                </ul>

                {errorReportListing}
            </div>);


        return (
            <CollapsiblePanel id="oai-record-panel" title="Record overview" collapsed={collapsed}
                          onTogglePanelCollapse={onTogglePanelCollapse}>
                {body}
            </CollapsiblePanel>
        );
    }
}

export default OaiRecordDashboard;

