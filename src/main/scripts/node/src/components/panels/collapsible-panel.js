import React from "react";

class CollapsiblePanel extends React.Component {

    render() {
        const { title } = this.props;
        return (
            <div className="panel panel-default">
                <div className="panel-heading">
                    {title}
                </div>
                <div className="panel-body">
                    {this.props.children}
                    <div className="clearfix" />
                </div>
            </div>
        );
    }
}

export default CollapsiblePanel;