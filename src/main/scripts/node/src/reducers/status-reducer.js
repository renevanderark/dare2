import ActionTypes from "../action-types";

const initialState = {
    status: {
        harvesterStatus: {
            harvesterRunState: null,
            recordFetcherRunState: null
        }
    }
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
                    records: getTotals(action.data["recordProcessingStatus"].recordStatus),
                    errors: getTotals(action.data["recordProcessingStatus"].errorStatus)
                }
            };
        default:
    }

    return state;
}