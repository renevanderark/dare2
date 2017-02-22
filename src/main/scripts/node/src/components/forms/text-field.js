import React from "react";

const TextField = ({label, value, onChange}) => (
    <div className="form-group row">
        <label className="col-md-4">{label}</label>
        <div className="col-md-27">
            <input className="form-control" type="text" value={value} onChange={onChange} />
        </div>
    </div>
);

TextField.propTypes = {
    label: React.PropTypes.string.isRequired,
    value: React.PropTypes.string.isRequired,
    onChange: React.PropTypes.func.isRequired
};

export default TextField;