import React from "react";

class InnerPanel extends React.Component {

    render() {
        const spacing = this.props.spacing || "col-md-6 col-sm-8 col-xs-10";
        return (
            <div className={spacing}>
                <div className="panel panel-default" style={{marginBottom: "0.33em"}}>
                    <div className="panel-body">
                        {this.props.children}
                    </div>
                </div>
            </div>
        );
    }
}

export default InnerPanel;