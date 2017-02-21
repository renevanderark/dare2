import React from "react";
import { Link } from "react-router";
import Workflow from "./dashboards/workflow";
import OaiRecords from "./dashboards/oai-records";
import ErrorReports from "./dashboards/error-reports";

class OaiRecord extends React.Component {

    componentDidMount() {
        if (!this.props.records.collapsed) {
           this.props.onSetRecordQueryFilter("repositoryId", this.props.params.id, this.props.params.id);
        }
    }

    render() {
        // actions for records
        const { onSetRecordQueryFilter, onRefetchRecords, onSetRecordQueryOffset } = this.props;

        // actions for panels
        const { onTogglePanelCollapse } = this.props;

        const workFlow = (
            <Workflow {...this.props.workflow}
                      onSetRecordQueryFilter={(field, value) => onSetRecordQueryFilter(field, value, this.props.params.id)}
                      onTogglePanelCollapse={onTogglePanelCollapse} />
        );
        const oaiRecords = (
            <OaiRecords {...this.props.records}
                        onSetRecordQueryFilter={onSetRecordQueryFilter}
                        onSetRecordQueryOffset={onSetRecordQueryOffset}
                        onTogglePanelCollapse={onTogglePanelCollapse}
                        onRefetchRecords={onRefetchRecords} />
        );

        const errorReports = (
            <ErrorReports {...this.props.errors}
                          onSetRecordQueryFilter={(field, value) => onSetRecordQueryFilter(field, value, this.props.params.id)}
                          onTogglePanelCollapse={onTogglePanelCollapse} />
        );
        return (
            <div>
                <ol className="breadcrumb">
                    <li><Link to="/">Dashboard</Link></li>
                    <li className="active">Data provider</li>
                </ol>
                {workFlow}
                {oaiRecords}
                {errorReports}
            </div>
        );
    }
}

export default OaiRecord;