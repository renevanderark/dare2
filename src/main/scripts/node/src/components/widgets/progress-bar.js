import React from "react";

const toPercentage = (current, total) =>
    `${parseInt((current / total) * 100, 10)}%`;

const ProgressBar = ({current, total, className, children}) => (
    <div className={`progress ${className}`} style={{marginBottom: 0}}>
        <div className={`progress-bar ${current < total ? "progress-bar-striped active" : ""}`}
             style={{width: toPercentage(current, total), opacity: 0.6 }} >
            
            <span style={{display: "inline-block", marginRight: 8}}>
                {children}
            </span>
            <span>
                {toPercentage(current, total)}
            </span>
        </div>
    </div>
);

ProgressBar.propTypes = {
    current: React.PropTypes.number.isRequired,
    total: React.PropTypes.number.isRequired,
    className: React.PropTypes.string
};

export default ProgressBar;