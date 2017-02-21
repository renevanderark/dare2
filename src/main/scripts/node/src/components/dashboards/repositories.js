import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";
import { Link } from "react-router";
import { urls } from "../../router";

class Repositories extends React.Component {

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
                            Set
                        </div>
                        <div className="col-md-14 col-sm-14 col-xs-14">
                            <span className="pull-right" title="...of an OAI record">Latest datestamp <sup>1</sup></span>
                        </div>
                        <div className="col-md-4 col-sm-4 col-xs-4">
                            <span className="pull-right">Options</span>
                        </div>
                    </li>
                    {this.props.list.map((repo, i) => (
                      <li className="list-group-item row" key={i}>
                          <div className="col-md-14 col-sm-14 col-xs-14">
                              <Link to={urls.dataProvider(repo.id)}>
                                {repo.set}
                              </Link>
                          </div>
                          <div className="col-md-14 col-sm-14 col-xs-14">
                              <span className="pull-right">
                                  {repo.dateStamp || "- none harvested yet -"}
                              </span>
                          </div>
                          <div className="col-md-4 col-sm-4 col-xs-4">
                              {repo.enabled
                                  ? (<button className="btn btn-default btn-xs pull-right"
                                             onClick={() => onDisableRepository(repo.id)}>
                                      <span className="glyphicon glyphicon-stop"/>
                                    </button>
                                  ) : (<button className="btn btn-default btn-xs pull-right"
                                               onClick={() => onEnableRepository(repo.id)}>
                                      <span className="glyphicon glyphicon-play"/>
                                    </button>
                                  )
                              }
                          </div>
                      </li>
                    ))}
                </ul>
            </CollapsiblePanel>
        )
    }
}

export default Repositories;