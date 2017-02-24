import { convertRecords, convertWorkerControls } from "./converters";


const dashboardsConnector = (state) => {
    return {
        workerControls: convertWorkerControls(state),
        workflow: {
            ...(state.status.totals || {}).records,
            collapsed: state.panels["workflow-panel"].collapsed
        },
        errors: {
            ...(state.status.totals || {}).errors,
            collapsed: state.panels["error-panel"].collapsed
        },
        records: convertRecords(state),
        repositories: {
            collapsed: state.panels["repositories-panel"].collapsed,
            list: state.repositories.list
        },
        status: state.status
    };
};


export default dashboardsConnector;