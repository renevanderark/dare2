import React from "react";
import WorkerControls from "./dashboards/worker-controls";
import Workflow from "./dashboards/workflow";

class DashBoards extends React.Component {

    render() {
        // actions for Workflow
        const { onStartOaiHarvester, onDisableOaiHarvester, onStartOaiRecordFetcher, onDisableOaiRecordFetcher } = this.props;

        return (
            <div>
                <WorkerControls {...this.props.workerControls}
                    onStartOaiHarvester={onStartOaiHarvester}
                    onDisableOaiHarvester={onDisableOaiHarvester}
                    onStartOaiRecordFetcher={onStartOaiRecordFetcher}
                    onDisableOaiRecordFetcher={onDisableOaiRecordFetcher}
                />
                <Workflow {...this.props.workflow} />
                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default DashBoards;