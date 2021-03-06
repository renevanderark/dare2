import React from "react";

class InnerPanelSpacer extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.spacing !== nextProps.spacing;
    }

    render() {
        const sp = this.props.spacing || "col-md-1 col-sm-1 col-xs-1";

        return (
            <div className={`${sp} text-center`}>
                {this.props.children}
            </div>
        );
    }
}

export default InnerPanelSpacer;