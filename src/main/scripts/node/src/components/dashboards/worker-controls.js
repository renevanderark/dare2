import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import InnerPanelSpacer from "../panels/inner-panel-spacer";

const lpad = number => number <= 99 ? ("0"+number).slice(-2) : number;


class WorkerControls extends React.Component {

    render() {
        const {
            recordFetcherRunState,
            harvesterRunState,
            nextRun
        } = this.props;


        const recordFetcherButton = recordFetcherRunState === "RUNNING"
            ? (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-play" /></button>);
        const harvesterButton = harvesterRunState === "RUNNING"
            ? (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right"><span className="glyphicon glyphicon-play" /></button>);

        return (
            <CollapsiblePanel title="Workers">
                <InnerPanel>
                    {harvesterButton}
                    Harvesters <br /> ({harvesterRunState})
                    <br />
                    next run {nextRun}
                </InnerPanel>
                <InnerPanelSpacer />
                <InnerPanel>
                    {recordFetcherButton}
                    Object harvester <br /> ({recordFetcherRunState}) <br />&nbsp;
                </InnerPanel>
            </CollapsiblePanel>
        )
    }
}

export default WorkerControls;