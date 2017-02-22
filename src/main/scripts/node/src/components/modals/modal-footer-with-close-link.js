import React from "react";
import { Link } from "react-router";

const ModalFooter  = ({children, closeLink, closeLinkLabel}) => (
    <div className="modal-footer">
        <Link to={closeLink} className="btn btn-default pull-left">
            {closeLinkLabel}
        </Link>
        {children}
    </div>
);

ModalFooter.defaultProps = {
    closeLinkLabel: "Cancel"
};

ModalFooter.propTypes = {
    closeLink: React.PropTypes.string.isRequired,
    closeLinkLabel: React.PropTypes.string,
};

export default ModalFooter;