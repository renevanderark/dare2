import React from "react";
import BreadCrumbs from "./layout/breadcrumbs";
import ProgressDashboard from "./dashboards/progress";

class Progress extends React.Component {

    render() {
        const { onTogglePanelCollapse } = this.props;
        return (
            <div className="container container-fluid">
                <BreadCrumbs titles={["Progress"]} />
                <ProgressDashboard {...this.props.progress}
                                   onTogglePanelCollapse={onTogglePanelCollapse}
                />
            </div>
        );
    }
}

export default Progress;