const lpad = number => number <= 99 ? ("0"+number).slice(-2) : number;

const getNextRun = (nextRunTime) => {
    const hours = lpad(parseInt(Math.floor(((nextRunTime / 1000) / 60) / 60), 10));
    const minutes = lpad(parseInt(Math.floor(((nextRunTime / 1000) / 60) % 60), 10));
    const seconds = lpad(parseInt(Math.floor(((nextRunTime / 1000) % 60) % 60), 10));
    return `${hours}:${minutes}:${seconds}`;
}

const dashboardsConnector = (state) => {
    const { status: {
        status: {
            harvesterStatus: {
                recordFetcherRunState,
                harvesterRunState,
                nextRunTime
            }
        }
    }} = state;

    return {
        workerControls: {
            nextRun: getNextRun(nextRunTime),
            recordFetcherRunState: recordFetcherRunState,
            harvesterRunState: harvesterRunState
        },
        workflow: (state.status.totals || {}).records,
        status: state.status
    };
};


export default dashboardsConnector;