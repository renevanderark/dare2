const convertRecords = (state) => {
    const repositories = state.repositories.list || [];

    const query = Object.keys(state.oaiRecords.query)
        .filter((key) => state.oaiRecords.query[key] !== null && key !== 'limit' && key !== 'offset')
        .map((key) => ({
            key: key,
            label: key === "repositoryId"
                ? (repositories.find(repo => "" + repo.id === state.oaiRecords.query[key]) || {}).name
                : state.oaiRecords.query[key]
        }));

    const { status: {
        status: {
            harvesterStatus: {
                recordFetcherRunState
            }
        }
    }} = state;

    return {
        results: {
            count: state.oaiRecords.results.count,
            result: (state.oaiRecords.results.result || []).map(record => ({
                ...record,
                repositoryName: (repositories.find(repo => repo.id === record.repositoryId) || {name: ""}).name
            }))
        },
        query: state.oaiRecords.query,
        labeledQuery: query,
        bulkResetEnabled: recordFetcherRunState === "DISABLED",
        collapsed: state.panels["oai-records-panel"].collapsed
    };
};

const lpad = number => number <= 99 ? ("0"+number).slice(-2) : number;

const getNextRun = (nextRunTime) => {
    const hours = lpad(parseInt(Math.floor(((nextRunTime / 1000) / 60) / 60), 10));
    const minutes = lpad(parseInt(Math.floor(((nextRunTime / 1000) / 60) % 60), 10));
    const seconds = lpad(parseInt(Math.floor(((nextRunTime / 1000) % 60) % 60), 10));
    return `${hours}:${minutes}:${seconds}`;
};


const convertWorkerControls = (state) => {
    const { status: {
        status: {
            harvesterStatus: {
                recordFetcherRunState,
                harvesterRunState,
                nextRunTime,
                currentRepository
            }
        }
    }} = state;
    console.log(JSON.stringify(state.status.status.recordsBeingProcessed));
    return {
        nextRun: getNextRun(nextRunTime),
        recordFetcherRunState: recordFetcherRunState,
        harvesterRunState: harvesterRunState,
        currentRepository: (currentRepository || {}).name,
        currentDateStamp: (currentRepository  || {}).dateStamp,
        collapsed: state.panels["workers-panel"].collapsed
    };
};

export { convertRecords, convertWorkerControls }