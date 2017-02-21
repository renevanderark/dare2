import React from "react";
import { Link } from "react-router";

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
                <ol className="breadcrumb">
                    <li><Link to="/">Dashboard</Link></li>
                    <li className="active">Record overview</li>
                </ol>
                {oaiRecordPanel}
                {oaiRecords}
            </div>
        );
    }
}

export default OaiRecord;