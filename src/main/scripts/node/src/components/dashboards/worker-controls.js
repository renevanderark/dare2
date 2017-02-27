import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import InnerPanelSpacer from "../panels/inner-panel-spacer";


class WorkerControls extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.collapsed !== nextProps.collapsed ||
                this.props.nextRun !== nextProps.nextRun ||
                this.props.harvesterRunState !== nextProps.harvesterRunState ||
                this.props.recordFetcherRunState !== nextProps.recordFetcherRunState ||
                this.props.currentRepository !== nextProps.currentRepository ||
                this.props.currentDateStamp !== nextProps.currentDateStamp;
    }

    render() {
        // states
        const { recordFetcherRunState, harvesterRunState, nextRun, currentRepository, currentDateStamp } = this.props;
        // worker control actions
        const { onStartOaiHarvester, onDisableOaiHarvester, onStartOaiRecordFetcher, onDisableOaiRecordFetcher } = this.props;
        // panel actions
        const { onTogglePanelCollapse } = this.props;

        const harvesterButton = harvesterRunState === "RUNNING"
            ? (<button className="btn btn-default pull-right" onClick={onDisableOaiHarvester}><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right" disabled={harvesterRunState === "DISABLING"} onClick={onStartOaiHarvester}><span className="glyphicon glyphicon-play" /></button>);

        const harvesterDisableButton = harvesterRunState === "WAITING"
            ? (<button className="btn btn-default pull-right" onClick={onDisableOaiHarvester}><span className="glyphicon glyphicon-remove" /></button>)
            : null;

        const recordFetcherButton = recordFetcherRunState === "RUNNING" || recordFetcherRunState === "WAITING"
            ? (<button className="btn btn-default pull-right" onClick={onDisableOaiRecordFetcher}><span className="glyphicon glyphicon-stop" /></button>)
            : (<button className="btn btn-default pull-right" disabled={recordFetcherRunState === "DISABLING"} onClick={onStartOaiRecordFetcher}><span className="glyphicon glyphicon-play" /></button>);

        const nextRunMessage = harvesterRunState === "WAITING"
            ? `Next: run: ${nextRun}`
            : null;

        const currentRepositoryMessage = currentRepository && currentDateStamp ?
            `${currentRepository} (${currentDateStamp})` : null;

        return (
            <CollapsiblePanel id="workers-panel" collapsed={this.props.collapsed} title="Workers"
                              onTogglePanelCollapse={onTogglePanelCollapse}>
                <InnerPanel>
                    {harvesterButton}{harvesterDisableButton}
                    Harvesters <br /> ({harvesterRunState})
                    <br />
                    {nextRunMessage || currentRepositoryMessage}
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