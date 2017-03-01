import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";

import RecordBody from "./oai-record/record-body";
import ErrorList from "./oai-record/error-list";
import OaiRecordTestResults from "./oai-record/test-results";

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
        const { oaiRecord: { record, collapsed, errorReports, repositoryName, testResults }, identifier } = this.props;
        const { onTogglePanelCollapse, onTestRecord } = this.props;

        const body = !record
            ? (<div>Loading: {identifier}</div>)
            : (<div>
                <RecordBody {...record}
                            testResultsPending={(testResults || {}).pending}
                            onTestRecord={onTestRecord} repositoryName={repositoryName} />
                <OaiRecordTestResults {...testResults} />
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

