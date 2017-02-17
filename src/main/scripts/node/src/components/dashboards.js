import React from "react";
import WorkerControls from "./dashboards/worker-controls";

class DashBoards extends React.Component {

    render() {

        return (
            <div>
                <WorkerControls {...this.props.workerControls} />
                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default DashBoards;