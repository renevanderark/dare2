import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import InnerPanelSpacer from "../panels/inner-panel-spacer";
import { numberFormat } from "../../util/format-number";
import CounterBadge from "./counter-badge";

class Workflow extends React.Component {

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
                <InnerPanel spacing="col-md-5 col-sm-5 col-xs-7">
                    <span title="Deleted by data provider after processing">Deleted<sup>1</sup></span>
                    <CounterBadge onSetRecordQueryFilter={onSetRecordQueryFilter}
                                  filterKey="processStatus"
                                  filterValue="deleted-after-processing"
                                  count={this.props["deleted-after-processing"]} />
                </InnerPanel>

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
                <InnerPanelSpacer spacing="col-md-14 col-sm-10 col-xs-4" />
                <InnerPanel spacing="col-md-5 col-sm-5 col-xs-7">
                    <span title="Updated by data provider after processing">Updated<sup>1</sup></span>
                    <CounterBadge onSetRecordQueryFilter={onSetRecordQueryFilter}
                                  filterKey="processStatus"
                                  filterValue="updated-after-processing"
                                  count={this.props["updated-after-processing"]} />
                </InnerPanel>
            </CollapsiblePanel>
        )
    }
}

export default Workflow;