import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import InnerPanel from "../panels/inner-panel";
import InnerPanelSpacer from "../panels/inner-panel-spacer";


class Workflow extends React.Component {

    render() {

        return (
            <CollapsiblePanel title="Processing status">
                <InnerPanel>
                    Pending <span className="badge pull-right">{this.props.pending || 0}</span>
                </InnerPanel>
                <InnerPanelSpacer>
                   <span className="glyphicon glyphicon-arrow-right" style={{top: "16px"}} />
                </InnerPanelSpacer>
                <InnerPanel>
                    Processing <span className="badge pull-right">{this.props.processing || 0}</span>
                </InnerPanel>
                <InnerPanelSpacer>
                    <span className="glyphicon glyphicon-arrow-right" style={{top: "16px"}} />
                </InnerPanelSpacer>
                <InnerPanel>
                    Processed <span className="badge pull-right">{this.props.processed || 0}</span>
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
                    Failure <span className="badge pull-right">{this.props.failure || 0}</span>
                </InnerPanel>
                <InnerPanelSpacer spacing="col-md-14 col-sm-10 col-xs-4" />
                <InnerPanel spacing="col-md-5 col-sm-5 col-xs-7">
                    <span title="Deleted by data provider">Skip <sup>1</sup></span>
                    <span className="badge pull-right">{this.props.skip || 0}</span>
                </InnerPanel>
            </CollapsiblePanel>
        )
    }
}

export default Workflow;