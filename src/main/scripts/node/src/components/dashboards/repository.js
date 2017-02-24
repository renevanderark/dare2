import React from "react";
import EnableToggle from "../widgets/enable-toggle";
import { Link } from "react-router";
import { urls } from "../../router";

class Repository extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.enabled !== nextProps.enabled ||
            this.props.id !== nextProps.id ||
            this.props.name !== nextProps.name ||
            this.props.dateStamp !== nextProps.dateStamp;

    }

    render() {
        // repository actions
        const { onEnableRepository, onDisableRepository } = this.props;

        const { name, dateStamp, id, enabled } = this.props;

        return (
            <li className="list-group-item row">
                <div className="col-md-14 col-sm-14 col-xs-14">
                    <Link to={urls.dataProvider(id)}>
                        {name}
                    </Link>
                </div>
                <div className="col-md-14 col-sm-14 col-xs-14">
                  <span className="pull-right">
                      {dateStamp || "- none harvested yet -"}
                  </span>
                </div>
                <div className="col-md-4 col-sm-4 col-xs-4">
                  <span className="pull-right">
                     <EnableToggle enabled={enabled}
                          onEnableClick={() => onEnableRepository(id)}
                          onDisableClick={() => onDisableRepository(id)} />
                  </span>
                </div>
            </li>

        );
    }
}

export default Repository;