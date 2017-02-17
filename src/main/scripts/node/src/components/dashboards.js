import React from "react";
import WorkerControls from "./dashboards/worker-controls";

class DashBoards extends React.Component {

    render() {

        return (
            <div>
                <WorkerControls {...this.props} />
            </div>
        )
    }
}

export default DashBoards;