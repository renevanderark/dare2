import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";

class OaiRecordDashboard extends React.Component {

    componentWillReceiveProps(nextProps) {
        const { onFetchOaiRecord } = this.props;

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
        const { oaiRecord: { record, collapsed, errorReports }, identifier } = this.props;
        const { onTogglePanelCollapse } = this.props;

        const body = !record
            ? (<div>Loading: {identifier}</div>)
            : (<div>
                <pre>{JSON.stringify(record, null, 2)}</pre>
                <pre>{JSON.stringify(errorReports, null, 2)}</pre>
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

