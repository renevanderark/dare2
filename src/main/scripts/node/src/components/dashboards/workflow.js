import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import InnerPanelSpacer from "../panels/inner-panel-spacer";
import CounterBadge from "../widgets/counter-badge";

class Workflow extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.pending !== nextProps.pending ||
                this.props.processing !== nextProps.processing ||
                this.props.processed !== nextProps.processed ||
                this.props.failure !== nextProps.failure;
    }

    render() {
        // panel actions
        const { onTogglePanelCollapse, onSetRecordQueryFilter } = this.props;

        return (
            <CollapsiblePanel id="workflow-panel" collapsed={this.props.collapsed} title="Processing status"
                              onTogglePanelCollapse={onTogglePanelCollapse}>
                <InnerPanel>
                    Pending
                    <CounterBadge onSetRecordQueryFilter={onSetRecordQueryFilter}
                                  filterKey="processStatus"
                                  filterValue="pending"
                                  count={this.props.pending} />
                </InnerPanel>
                <InnerPanelSpacer>
                   <span className="glyphicon glyphicon-arrow-right" style={{top: "16px"}} />
                </InnerPanelSpacer>
                <InnerPanel>
                    Processing
                    <CounterBadge onSetRecordQueryFilter={onSetRecordQueryFilter}
                                  filterKey="processStatus"
                                  filterValue="processing"
                                  count={this.props.processing} />
                </InnerPanel>
                <InnerPanelSpacer>
                    <span className="glyphicon glyphicon-arrow-right" style={{top: "16px"}} />
                </InnerPanelSpacer>
                <InnerPanel>
                    Processed
                    <CounterBadge onSetRecordQueryFilter={onSetRecordQueryFilter}
                                  filterKey="processStatus"
                                  filterValue="processed"
                                  count={this.props.processed} />
                </InnerPanel>

                <InnerPanelSpacer spacing="col-md-7 col-sm-1 col-xs-1" />


                <div className="clearfix" />

                <div className="row">
                    <InnerPanelSpacer spacing="col-md-9 col-sm-12 col-xs-15" />
                    <InnerPanelSpacer>
                        <span className="glyphicon glyphicon-arrow-down" />
                    </InnerPanelSpacer>
                </div>


                <InnerPanelSpacer spacing="col-md-7 col-sm-9 col-xs-11" />
                <InnerPanel>
                    Failure
                    <CounterBadge onSetRecordQueryFilter={onSetRecordQueryFilter}
                                  filterKey="processStatus"
                                  filterValue="failure"
                                  count={this.props.failure} />
                </InnerPanel>

            </CollapsiblePanel>
        )
    }
}

export default Workflow;