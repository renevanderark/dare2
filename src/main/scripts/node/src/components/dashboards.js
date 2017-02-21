import React from "react";
import WorkerControls from "./dashboards/worker-controls";
import Workflow from "./dashboards/workflow";
import ErrorReports from "./dashboards/error-reports";
import Repositories from "./dashboards/repositories";
import OaiRecords from "./dashboards/oai-records";

class DashBoards extends React.Component {

    render() {
        // actions for WorkerControls
        const { onStartOaiHarvester, onDisableOaiHarvester, onStartOaiRecordFetcher, onDisableOaiRecordFetcher } = this.props;

        // repository actions
        const { onEnableRepository, onDisableRepository } = this.props;

        // actions for records
        const { onSetRecordQueryFilter, onRefetchRecords, onSetRecordQueryOffset } = this.props;

        // actions for panels
        const { onTogglePanelCollapse } = this.props;

        const repositories = (
            <Repositories {...this.props.repositories}
                onTogglePanelCollapse={onTogglePanelCollapse}
                onEnableRepository={onEnableRepository}
                onDisableRepository={onDisableRepository}
            />
        );

        const workerControls = (
            <WorkerControls {...this.props.workerControls}
                onStartOaiHarvester={onStartOaiHarvester}
                onDisableOaiHarvester={onDisableOaiHarvester}
                onStartOaiRecordFetcher={onStartOaiRecordFetcher}
                onDisableOaiRecordFetcher={onDisableOaiRecordFetcher}
                onTogglePanelCollapse={onTogglePanelCollapse}
            />);

        const workFlow = (
            <Workflow {...this.props.workflow}
                onSetRecordQueryFilter={onSetRecordQueryFilter}
                onTogglePanelCollapse={onTogglePanelCollapse} />
        );

        const errorReports = (
            <ErrorReports {...this.props.errors}
                          onSetRecordQueryFilter={onSetRecordQueryFilter}
                          onTogglePanelCollapse={onTogglePanelCollapse} />
        );

        const oaiRecords = (
            <OaiRecords {...this.props.records}
                        onSetRecordQueryFilter={onSetRecordQueryFilter}
                        onSetRecordQueryOffset={onSetRecordQueryOffset}
                        onTogglePanelCollapse={onTogglePanelCollapse}
                        onRefetchRecords={onRefetchRecords} />
        );

        return (
            <div>
                <ol className="breadcrumb">
                    <li className="active">Dashboard</li>
                </ol>

                {repositories }
                {workerControls }
                {workFlow}
                {oaiRecords}
                {errorReports}
                <div className="clearfix" />
                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default DashBoards;