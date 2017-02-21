import React from "react";
import CollapsiblePanel from "./panels/collapsible-panel";

class OaiRecord extends React.Component {

    componentDidMount() {
        const { oaiRecord, identifier, onFetchOaiRecord } = this.props;

        if (oaiRecord === null || oaiRecord.record.identifier !== identifier) {
            onFetchOaiRecord(identifier);
        }
    }

    render() {
        const { oaiRecord, identifier, collapsed } = this.props;
        const { onTogglePanelCollapse } = this.props;

        const oaiRecordBody = oaiRecord === null
            ? (<div>Loading: {identifier}</div>)
            : (<pre>{JSON.stringify(oaiRecord, null, 2)}</pre>);

        return (
            <div>
                <CollapsiblePanel id="oai-record-panel" title="Record overview" collapsed={collapsed}
                                  onTogglePanelCollapse={onTogglePanelCollapse}>
                    {oaiRecordBody}
                </CollapsiblePanel>
            </div>
        );
    }
}

export default OaiRecord;