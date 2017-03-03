import React from "react";
import ValidationMarker from "../../../widgets/validation-marker";

class ProgressStep extends React.Component {

    render() {
        return (
            <li className="list-group-item row">
                <span className="col-md-8 col-sm-12 col-xs-15">
                    {this.props.title}
                </span>
                <span className="col-md-1 col-sm-1 col-xs-1">
                  <ValidationMarker validates={this.props.validates}
                                    messageOk={this.props.messageOk}
                                    messageFail={this.props.messageFail}
                  />
                </span>
                {this.props.children}
            </li>
        );
    }
}

ProgressStep.propTypes = {
    title: React.PropTypes.string.isRequired,
    validates: React.PropTypes.bool.isRequired,
    messageOk: React.PropTypes.string.isRequired,
    messageFail: React.PropTypes.string.isRequired
};

export default ProgressStep;