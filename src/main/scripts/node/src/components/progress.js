import React from "react";
import BreadCrumbs from "./layout/breadcrumbs";
import ProgressDashboard from "./dashboards/progress";
import Workflow from "./dashboards/workflow";

class Progress extends React.Component {

    render() {
        const { onTogglePanelCollapse } = this.props;

        return (
            <div className="container container-fluid">
                <BreadCrumbs titles={["Progress"]} />
                <Workflow {...this.props.workflow} key="workflow"
                          onSetRecordQueryFilter={() => {}}
                          onTogglePanelCollapse={onTogglePanelCollapse}
                />
                <ProgressDashboard {...this.props.progress}
                                   onTogglePanelCollapse={onTogglePanelCollapse}
                />
            </div>
        );
    }
}

export default Progress;