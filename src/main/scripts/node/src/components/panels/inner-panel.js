import React from "react";

class InnerPanel extends React.Component {

    render() {
        return (
            <div className="col-md-6 col-sm-8 col-xs-10">
                <div className="panel panel-default">
                    <div className="panel-body">
                        {this.props.children}
                    </div>
                </div>
            </div>
        );
    }
}

export default InnerPanel;