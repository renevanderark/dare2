import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import Repository from "./repository";

const serializeProps = (props) =>
    props.map(({id, enabled, name, dateStamp}) => `${id}${enabled}${name}${dateStamp}`)
        .join();

class Repositories extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.collapsed !== nextProps.collapsed ||
                serializeProps(nextProps.list) !== serializeProps(this.props.list);

    }

    render() {
        // repository actions
        const { onEnableRepository, onDisableRepository } = this.props;

        // panel actions
        const { onTogglePanelCollapse } = this.props;

        return (
            <CollapsiblePanel id="repositories-panel" collapsed={this.props.collapsed} title="Data providers"
                              onTogglePanelCollapse={onTogglePanelCollapse}>

                <ul className="list-group" style={{overflowY: "auto", maxHeight: "200px"}}>
                    <li className="list-group-item row">
                        <div className="col-md-14 col-sm-14 col-xs-14">
                            Name
                        </div>
                        <div className="col-md-14 col-sm-14 col-xs-14">
                            <span className="pull-right" title="...of an OAI record">Latest datestamp <sup>1</sup></span>
                        </div>
                        <div className="col-md-4 col-sm-4 col-xs-4">
                            <span className="pull-right">Options</span>
                        </div>
                    </li>
                    {this.props.list.map((repo, i) => (
                        <Repository {...repo} key={i}
                                onEnableRepository={onEnableRepository}
                                onDisableRepository={onDisableRepository} />
                    ))}
                </ul>
            </CollapsiblePanel>
        )
    }
}

export default Repositories;