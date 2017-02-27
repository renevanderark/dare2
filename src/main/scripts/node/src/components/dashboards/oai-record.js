import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";

import RecordBody from "./oai-record/record-body";
import ErrorList from "./oai-record/error-list";

class OaiRecordDashboard extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.identifier !== nextProps.identifier ||
                this.props.oaiRecord !== nextProps.oaiRecord;
    }

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
        const { oaiRecord: { record, collapsed, errorReports, repositoryName }, identifier } = this.props;
        const { onTogglePanelCollapse } = this.props;


        const body = !record
            ? (<div>Loading: {identifier}</div>)
            : (<div>
                <RecordBody {...record} repositoryName={repositoryName} />
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

