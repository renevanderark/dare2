import React from "react";

class CollapsiblePanel extends React.Component {

    render() {
        const { id, title, collapsed, onTogglePanelCollapse } = this.props;

        const panelBody = collapsed ? null : (
            <div className="panel-body">
                {this.props.children}
                <div className="clearfix" />
            </div>
        );

        return (
            <div className={collapsed ? "collapsed-panel" : ""}>
                {collapsed ? null : <div className="clearfix" />}
                <div className={`panel panel-default ${collapsed ? "col-md-6 col-sm-8 col-xs-16" : ""}`}>
                    <div className="panel-heading">
                        {title}
                        <span className="pull-right" style={{cursor: "pointer"}} onClick={() => onTogglePanelCollapse(id)}>
                            <span className={`glyphicon glyphicon-collapse-${collapsed ? "down" : "up"}`} />
                        </span>
                    </div>
                    {panelBody}
                </div>
            </div>
        );
    }
}

CollapsiblePanel.propTypes = {
    id: React.PropTypes.string.isRequired,
    collapsed: React.PropTypes.bool.isRequired
};

export default CollapsiblePanel;