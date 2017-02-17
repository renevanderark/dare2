import React from "react";
import WorkerControls from "./dashboards/worker-controls";


class App extends React.Component {

    render() {

        return (
            <div className="container container-fluid">
                <WorkerControls {...this.props} />
                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default App;