import React from "react";

class ValidationMarker extends React.Component {

    render() {
        const { validates, messageOk, messageFail } = this.props;

        return typeof validates === 'undefined'
            ? null
            : validates
                ? <span title={messageOk} className="glyphicon glyphicon-ok pull-right"
                        style={{color: "green", cursor: "pointer"}} />
                : <span title={messageFail} className="glyphicon glyphicon-remove pull-right"
                        style={{color: "red", cursor: "pointer"}} />;
    }
}

ValidationMarker.propTypes = {
    validates: React.PropTypes.bool,
    messageOk: React.PropTypes.string.isRequired,
    messageFail: React.PropTypes.string.isRequired
};

export default ValidationMarker;