import React from "react";

const lpad = number => number <= 99 ? ("0"+number).slice(-2) : number;

class App extends React.Component {

    render() {


        const { status: {
            status: {
                harvesterStatus: {
                    recordFetcherRunState,
                    harvesterRunState,
                    nextRunTime
                }
            }
        }} = this.props;

        const hours = lpad(parseInt(Math.floor(((nextRunTime / 1000) / 60) / 60), 10));
        const minutes = lpad(parseInt(Math.floor(((nextRunTime / 1000) / 60) % 60), 10));
        const seconds = lpad(parseInt(Math.floor(((nextRunTime / 1000) % 60) % 60), 10));

        const recordFetcherButton = recordFetcherRunState === "RUNNING"
            ? (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-play" /></button>);
        const harvesterButton = harvesterRunState === "RUNNING"
            ? (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-play" /></button>);

        return (
            <div className="container container-fluid">
                <div className="panel panel-default">
                    <div className="panel-heading">
                        Workflow
                    </div>
                    <div className="panel-body col-md-3">
                            <div className="panel panel-default">
                                <div className="panel-body">
                                    {harvesterButton}
                                    Harvesters ({harvesterRunState})
                                    <br />
                                    next run {hours}:{minutes}:{seconds}
                                </div>
                        </div>
                    </div>
                    <div className="panel-body col-md-3">
                            <div className="panel panel-default">
                                <div className="panel-body">
                                    {recordFetcherButton}
                                    Object harvester ({recordFetcherRunState})
                                </div>
                            </div>
                    </div>
                    <div className="clearfix" />
                </div>
                <pre>
                    {JSON.stringify(this.props.status, null, 2)}
                </pre>
            </div>
        )
    }
}

export default App;