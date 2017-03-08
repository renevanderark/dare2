import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";

import RecordBody from "./oai-record/record-body";
import ErrorList from "./oai-record/error-list";
import OaiRecordTestResults from "./oai-record/test-results";
import Manifest from "./oai-record/manifest";

class OaiRecordDashboard extends React.Component {

    componentWillReceiveProps(nextProps) {
        const {onFetchOaiRecord} = this.props;

        if (nextProps.identifier !== this.props.identifier) {
            onFetchOaiRecord(nextProps.identifier);
        }
    }

    componentDidMount() {
        const { oaiRecord : { record }, identifier, onFetchOaiRecord } = this.props;

        if (!record || record.identifier !== identifier) {
            onFetchOaiRecord(identifier);
        }
    }

    render() {
        const { oaiRecord: { record, collapsed, errorReports, repositoryName, testResults, manifest }, identifier } = this.props;
        const { onTogglePanelCollapse, onTestRecord, onResetRecord, onFetchOaiRecord, onFetchManifest } = this.props;

        const body = !record
            ? (<div>Loading: {identifier}</div>)
            : (<div>
                <RecordBody {...record}
                            testResultsPending={(testResults || {}).pending}
                            onFetchOaiRecord={onFetchOaiRecord}
                            onFetchManifest={onFetchManifest}
                            onTestRecord={onTestRecord}
                            onResetRecord={onResetRecord}
                            repositoryName={repositoryName} />
                <OaiRecordTestResults {...testResults} />
                <Manifest manifest={manifest || []} identifier={record.identifier} />
                <ErrorList recordIdentifier={record.identifier} errorReports={errorReports} />
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

