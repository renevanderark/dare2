import React from "react";
import { Link } from "react-router";

const Modal  = ({closeLink, title, children}) => (
    <div className="modal show" style={{backgroundColor: "#0002"}}>
        <div className="modal-lg modal-dialog">
            <div className="modal-content">
                <div className="modal-header">
                    <Link to={closeLink} className="close">
                        &times;
                    </Link>
                    <h4 className="modal-title">{title}</h4>
                </div>
                {children}

            </div>
        </div>
    </div>
);

Modal.propTypes = {
    closeLink: React.PropTypes.string.isRequired,
    title: React.PropTypes.string.isRequired
};

export default Modal;