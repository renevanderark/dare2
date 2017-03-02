import React from "react";
import ModalCb from "./modal-with-close-callback";

class ButtonWithModalWarning extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            dialogueOpen: false
        }
    }

    closeDialogue() {
        this.setState({dialogueOpen: false});
    }

    render() {
        const { dialogueOpen } = this.state;

        return  dialogueOpen ? (
            <ModalCb  title={this.props.label} closeCallback={() => this.setState({dialogueOpen: false})}>
                <div className="modal-body">
                    {this.props.children}
                </div>
                <div className="modal-footer">
                    <button className={this.props.className} onClick={() => this.props.onConfirm(this.closeDialogue.bind(this))}>
                        {this.props.label}
                    </button>
                    <button className="btn btn-default pull-right" onClick={() => this.setState({dialogueOpen: false})}>
                        Cancel
                    </button>

                </div>
            </ModalCb>
        ) : <button className={this.props.className} onClick={() => this.setState({dialogueOpen: true})}>
                {this.props.label}
            </button>
    }
}

ButtonWithModalWarning.propTypes = {
    className: React.PropTypes.string.isRequired,
    label: React.PropTypes.string.isRequired,
    onConfirm: React.PropTypes.func.isRequired
};

export default ButtonWithModalWarning;