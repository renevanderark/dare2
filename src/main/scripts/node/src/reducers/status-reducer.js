import ActionTypes from "../action-types";

const initialState = {
};

function getTotals(data) {
    let recordTotals = {};

    for (let k in data) {
        for (let status in data[k]) {
            recordTotals[status] = recordTotals[status] || 0;
            recordTotals[status] += data[k][status];
        }
    }
    return recordTotals;
}

export default function(state=initialState, action) {
    switch (action.type) {
        case ActionTypes.ON_STATUS_UPDATE:
            return {
                status: action.data,
                totals: {
                    records: getTotals(action.data["record processing"].recordStatus),
                    errors: getTotals(action.data["record processing"].errorStatus)
                }
            };
        default:
    }

    return state;
}