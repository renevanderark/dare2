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
        const { onSetRecordQueryFilter, onRefetchRecords, onSetRecordQueryOffset, onResetRecords } = this.props;

        // actions for panels
        const { onTogglePanelCollapse } = this.props;

        return (
            <div className="container container-fluid" key="dashboards">
                <BreadCrumbs />
                <Repositories {...this.props.repositories} key="repositories"
                    onTogglePanelCollapse={onTogglePanelCollapse}
                    onEnableRepository={onEnableRepository}
                    onDisableRepository={onDisableRepository}
                />
                <WorkerControls {...this.props.workerControls} key="worker-controls"
                    onStartOaiHarvester={onStartOaiHarvester}
                    onDisableOaiHarvester={onDisableOaiHarvester}
                    onStartOaiRecordFetcher={onStartOaiRecordFetcher}
                    onDisableOaiRecordFetcher={onDisableOaiRecordFetcher}
                    onTogglePanelCollapse={onTogglePanelCollapse}
                />
                <Workflow {...this.props.workflow} key="workflow"
                    onSetRecordQueryFilter={onSetRecordQueryFilter}
                    onTogglePanelCollapse={onTogglePanelCollapse}
                />
                <OaiRecords {...this.props.records} key="oai-records"
                            onSetRecordQueryFilter={onSetRecordQueryFilter}
                            onSetRecordQueryOffset={onSetRecordQueryOffset}
                            onTogglePanelCollapse={onTogglePanelCollapse}
                            onRefetchRecords={onRefetchRecords}
                            onResetRecords={onResetRecords}
                />
                <ErrorReports {...this.props.errors} key="error-reports"
                      onSetRecordQueryFilter={onSetRecordQueryFilter}
                      onTogglePanelCollapse={onTogglePanelCollapse}
                />
            </div>
        )
    }
}

export default DashBoards;