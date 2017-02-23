import React from "react";
import WorkerControls from "./dashboards/worker-controls";
import Workflow from "./dashboards/workflow";
import ErrorReports from "./dashboards/error-reports";
import Repositories from "./dashboards/repositories";
import OaiRecords from "./dashboards/oai-records";
import BreadCrumbs from "./layout/breadcrumbs";

class DashBoards extends React.Component {
    componentDidMount() {
        if (!this.props.records.collapsed) {
            this.props.onSetRecordQueryFilter("repositoryId", null, null);
        }
    }

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
            <Repositories {...this.props.repositories} key="repositories"
                onTogglePanelCollapse={onTogglePanelCollapse}
                onEnableRepository={onEnableRepository}
                onDisableRepository={onDisableRepository}
            />
        );

        const workerControls = (
            <WorkerControls {...this.props.workerControls} key="worker-controls"
                onStartOaiHarvester={onStartOaiHarvester}
                onDisableOaiHarvester={onDisableOaiHarvester}
                onStartOaiRecordFetcher={onStartOaiRecordFetcher}
                onDisableOaiRecordFetcher={onDisableOaiRecordFetcher}
                onTogglePanelCollapse={onTogglePanelCollapse}
            />);

        const workFlow = (
            <Workflow {...this.props.workflow} key="workflow"
                onSetRecordQueryFilter={onSetRecordQueryFilter}
                onTogglePanelCollapse={onTogglePanelCollapse} />
        );

        const errorReports = (
            <ErrorReports {...this.props.errors} key="error-reports"
                          onSetRecordQueryFilter={onSetRecordQueryFilter}
                          onTogglePanelCollapse={onTogglePanelCollapse} />
        );

        const oaiRecords = (
            <OaiRecords {...this.props.records} key="oai-records"
                        onSetRecordQueryFilter={onSetRecordQueryFilter}
                        onSetRecordQueryOffset={onSetRecordQueryOffset}
                        onTogglePanelCollapse={onTogglePanelCollapse}
                        onRefetchRecords={onRefetchRecords} />
        );

        return (
            <div className="container container-fluid" key="dashboards">
                <BreadCrumbs />
                {repositories}
                {workerControls }
                {workFlow}
                {oaiRecords}
                {errorReports}
            </div>
        )
    }
}

export default DashBoards;