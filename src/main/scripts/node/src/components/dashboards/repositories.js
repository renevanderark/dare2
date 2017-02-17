import React from "react";
import CollapsiblePanel from "../panels/collapsible-panel";


class Repositories extends React.Component {

    render() {
        // repository actions
        const { onEnableRepository, onDisableRepository } = this.props;

        // panel actions
        const { onTogglePanelCollapse } = this.props;

        return (
            <CollapsiblePanel id="repositories-panel" collapsed={this.props.collapsed} title="Data providers"
                              onTogglePanelCollapse={onTogglePanelCollapse}>

                <div className="row">
                    <div className="col-md-14 col-sm-14 col-xs-14">
                        <strong>Set</strong>
                    </div>
                    <div className="col-md-14 col-sm-14 col-xs-14">
                        <strong className="pull-right" title="...of an OAI record">Latest datestamp <sup>1</sup></strong>
                    </div>
                    <div className="col-md-4 col-sm-4 col-xs-4">
                        <strong className="pull-right">Options</strong>
                    </div>
                </div>
                <div style={{overflowY: "auto", maxHeight: "150px"}}>
                    {this.props.list.map((repo, i) => (
                      <div className="row" style={{marginTop: "0.5em"}} key={i}>
                          <div className="col-md-14 col-sm-14 col-xs-14">
                              {repo.set}
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
                      </div>
                    ))}
                </div>
            </CollapsiblePanel>
        )
    }
}

export default Repositories;