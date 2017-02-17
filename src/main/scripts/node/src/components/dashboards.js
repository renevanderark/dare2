import React from "react";
import WorkerControls from "./dashboards/worker-controls";
import Workflow from "./dashboards/workflow";

class DashBoards extends React.Component {

    render() {
        // actions for WorkerControls
        const { onStartOaiHarvester, onDisableOaiHarvester, onStartOaiRecordFetcher, onDisableOaiRecordFetcher } = this.props;

        // actions for panels
        const { onTogglePanelCollapse } = this.props;

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
                      onTogglePanelCollapse={onTogglePanelCollapse}
            />
        );

        return (
            <div>
                {this.props.workerControls.collapsed ? workerControls : null}
                {this.props.workflow.collapsed ? workFlow: null}

                <div className="clearfix" />

                {this.props.workerControls.collapsed ? null : workerControls}
                {this.props.workflow.collapsed ? null: workFlow}

                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default DashBoards;