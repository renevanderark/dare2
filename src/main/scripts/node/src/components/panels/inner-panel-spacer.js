import React from "react";

class InnerPanelSpacer extends React.Component {

    render() {
        const sp = this.props.spacing || "col-md-1 col-sm-1 col-xs-1";

        const spSm = sp === 1 ? 1 : parseInt(Math.floor((sp / 6) * 8), 10);
        const spXs = sp === 1 ? 1 : parseInt(Math.floor((sp / 6) * 10), 10);

        return (
            <div className={`${sp} text-center`}>
                {this.props.children}
            </div>
        );
    }
}

export default InnerPanelSpacer;