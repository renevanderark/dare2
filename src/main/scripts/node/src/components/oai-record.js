import React from "react";
import BreadCrumbs from "./layout/breadcrumbs";

import OaiRecords from "./dashboards/oai-records";
import OaiRecordDashboard from "./dashboards/oai-record";

class OaiRecord extends React.Component {



    render() {
        const { oaiRecord, identifier, records } = this.props;
        const { onTogglePanelCollapse } = this.props;

        // actions for records
        const {
            onSetRecordQueryFilter,
            onRefetchRecords,
            onSetRecordQueryOffset,
            onFetchOaiRecord,
            onFetchManifest,
            onTestRecord,
            onResetRecord,
            onResetRecords
        } = this.props;

        const oaiRecordPanel =
            <OaiRecordDashboard identifier={identifier} oaiRecord={oaiRecord}
                                onFetchOaiRecord={onFetchOaiRecord}
                                onFetchManifest={onFetchManifest}
                                onTestRecord={onTestRecord}
                                onResetRecord={onResetRecord}
                                onTogglePanelCollapse={onTogglePanelCollapse} />;

        const oaiRecords = (
            <OaiRecords {...records}
                        onSetRecordQueryFilter={onSetRecordQueryFilter}
                        onSetRecordQueryOffset={onSetRecordQueryOffset}
                        onTogglePanelCollapse={onTogglePanelCollapse}
                        onRefetchRecords={onRefetchRecords}
                        onResetRecords={onResetRecords}
                        activeRecordIdentifier={identifier}
            />
        );

        return (
            <div className="container container-fluid">
                <BreadCrumbs titles={["Record overview"]} />
                {oaiRecordPanel}
                {oaiRecords}
            </div>
        );
    }
}

export default OaiRecord;