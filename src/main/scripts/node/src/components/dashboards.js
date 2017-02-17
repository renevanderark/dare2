import React from "react";
import WorkerControls from "./dashboards/worker-controls";

class DashBoards extends React.Component {

    render() {
        // actions for WorkerControls
        const { onStartOaiHarvester, onDisableOaiHarvester, onStartOaiRecordFetcher, onDisableOaiRecordFetcher } = this.props;

        return (
            <div>
                <WorkerControls {...this.props.workerControls}
                    onStartOaiHarvester={onStartOaiHarvester}
                    onDisableOaiHarvester={onDisableOaiHarvester}
                    onStartOaiRecordFetcher={onStartOaiRecordFetcher}
                    onDisableOaiRecordFetcher={onDisableOaiRecordFetcher}
                />
                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default DashBoards;