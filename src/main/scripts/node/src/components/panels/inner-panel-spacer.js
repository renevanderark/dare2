import React from "react";

class InnerPanelSpacer extends React.Component {

    render() {
        return (
            <div className="col-md-1 col-sm-1 col-xs-1">
                {this.props.children}
            </div>
        );
    }
}

export default InnerPanelSpacer;