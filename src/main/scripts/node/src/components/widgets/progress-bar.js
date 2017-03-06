import React from "react";
import styles from "../../util/styles";

const toPercentage = (current, total) =>
    total <= current ? "" :
    `${parseInt((current / total) * 100, 10)}%`;

const ProgressBar = ({current, total, className, children, indicator}) => (
    <div className={`progress ${className}`} style={{marginBottom: 0}}>
        <div className={`progress-bar ${current < total || total < current ? "progress-bar-striped active" : ""}`}
             style={{paddingTop: current < total ? "0.5px" : 0, width: toPercentage(current, total) || "100%", opacity: 0.6 }} >
            <span style={{display: "inline-block", marginRight: 8, textShadow: "0px 1px 0px rgba(255,255,255,.3), 0px -1px 0px rgba(0,0,0,.7)"}}>
                {children}
            </span>
            <span style={styles.textShadow}>
                {indicator(current, total)}
            </span>
        </div>
    </div>
);

ProgressBar.defaultProps = {
    indicator: toPercentage
};

ProgressBar.propTypes = {
    current: React.PropTypes.number.isRequired,
    total: React.PropTypes.number.isRequired,
    className: React.PropTypes.string,
    indicator: React.PropTypes.func
};

export default ProgressBar;