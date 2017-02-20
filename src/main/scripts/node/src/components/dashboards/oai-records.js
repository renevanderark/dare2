import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import { numberFormat } from "../../util/format-number";

class OaiRecords extends React.Component {

    render() {
        // panel actions
        const { onTogglePanelCollapse } = this.props;


        return (
            <CollapsiblePanel id="oai-records-panel" collapsed={this.props.collapsed} title="Records browser"
                              onTogglePanelCollapse={onTogglePanelCollapse}>

            </CollapsiblePanel>
        )
    }
}

export default OaiRecords;