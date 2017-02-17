import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import InnerPanelSpacer from "../panels/inner-panel-spacer";

const lpad = number => number <= 99 ? ("0"+number).slice(-2) : number;


class WorkerControls extends React.Component {

    render() {
        // states
        const { recordFetcherRunState, harvesterRunState, nextRun } = this.props;
        // actions
        const { onStartOaiHarvester, onDisableOaiHarvester, onStartOaiRecordFetcher, onDisableOaiRecordFetcher } = this.props;


        const recordFetcherButton = recordFetcherRunState === "RUNNING"
            ? (<button className="btn btn-default pull-right" onClick={onDisableOaiRecordFetcher}><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right" onClick={onStartOaiRecordFetcher}><span className="glyphicon glyphicon-play" /></button>);

        const harvesterButton = harvesterRunState === "RUNNING"
            ? (<button className="btn btn-default pull-right" onClick={onDisableOaiHarvester}><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right" onClick={onStartOaiHarvester}><span className="glyphicon glyphicon-play" /></button>);

        const nextRunMessage = harvesterRunState === "RUNNING"
            ? (<span>&nbsp;</span>)
            : `Next: run: ${nextRun}`;

        return (
            <CollapsiblePanel title="Workers">
                <InnerPanel>
                    {harvesterButton}
                    Harvesters <br /> ({harvesterRunState})
                    <br />
                    {nextRunMessage}
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