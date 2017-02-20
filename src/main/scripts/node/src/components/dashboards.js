import React from "react";
import WorkerControls from "./dashboards/worker-controls";
import Workflow from "./dashboards/workflow";
import ErrorReports from "./dashboards/error-reports";
import Repositories from "./dashboards/repositories";

class DashBoards extends React.Component {

    render() {
        // actions for WorkerControls
        const { onStartOaiHarvester, onDisableOaiHarvester, onStartOaiRecordFetcher, onDisableOaiRecordFetcher } = this.props;

        // repository actions
        const { onEnableRepository, onDisableRepository } = this.props;

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
            <Workflow {...this.props.workflow} onTogglePanelCollapse={onTogglePanelCollapse} />
        );

        const errorReports = (
            <ErrorReports {...this.props.errors} onTogglePanelCollapse={onTogglePanelCollapse} />
        );

        return (
            <div>
                {this.props.repositories.collapsed ? repositories : null}
                {this.props.workerControls.collapsed ? workerControls : null}
                {this.props.workflow.collapsed ? workFlow: null}
                {this.props.errors.collapsed ? errorReports: null}

                <div className="clearfix" />
                {this.props.repositories.collapsed ? null : repositories}
                {this.props.workerControls.collapsed ? null : workerControls}
                {this.props.workflow.collapsed ? null: workFlow}
                {this.props.errors.collapsed ? null: errorReports}

                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default DashBoards;