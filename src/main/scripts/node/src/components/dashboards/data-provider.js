import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";


class DataProviderDashboard extends React.Component {

    render() {
        const { collapsed } = this.props;
        const { onTogglePanelCollapse } = this.props;

        return (
            <CollapsiblePanel id="data-provider-panel" title="Data provider" collapsed={collapsed}
                              onTogglePanelCollapse={onTogglePanelCollapse}>
                foo
            </CollapsiblePanel>
        );
    }
}

export default DataProviderDashboard;