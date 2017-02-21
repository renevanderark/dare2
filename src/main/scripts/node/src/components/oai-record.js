import React from "react";
import CollapsiblePanel from "./panels/collapsible-panel";

import OaiRecords from "./dashboards/oai-records";
import OaiRecordDashboard from "./dashboards/oai-record";

class OaiRecord extends React.Component {



    render() {
        const { oaiRecord, identifier } = this.props;
        const { onTogglePanelCollapse } = this.props;

        // actions for records
        const { onSetRecordQueryFilter, onRefetchRecords, onSetRecordQueryOffset, onFetchOaiRecord } = this.props;

        const oaiRecordPanel =
            <OaiRecordDashboard identifier={identifier} oaiRecord={oaiRecord} onFetchOaiRecord={onFetchOaiRecord}
                                onTogglePanelCollapse={onTogglePanelCollapse} />;

        const oaiRecords = (
            <OaiRecords {...this.props.records}
                        onSetRecordQueryFilter={onSetRecordQueryFilter}
                        onSetRecordQueryOffset={onSetRecordQueryOffset}
                        onTogglePanelCollapse={onTogglePanelCollapse}
                        onRefetchRecords={onRefetchRecords}
                        activeRecordIdentifier={identifier}
            />
        );

        return (
            <div>
                {oaiRecordPanel}
                {oaiRecords}
            </div>
        );
    }
}

export default OaiRecord;