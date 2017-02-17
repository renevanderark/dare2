import xhr from "xhr";


const startOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/start", "method": "PUT"}, (err, resp, body) => {});

const disableOaiRecordFetcher = () => (dispatch) =>
    xhr({url: "/workers/disable", "method": "PUT"}, (err, resp, body) => {});

const startOaiHarvester = () => (dispatch) =>
    xhr({url: "/harvesters/start", "method": "PUT"}, (err, resp, body) => {});

const disableOaiHarvester = () => (dispatch) =>
    xhr({url: "/harvesters/disable", "method": "PUT"}, (err, resp, body) => {});


export {
    startOaiRecordFetcher,
    disableOaiRecordFetcher,
    disableOaiHarvester,
    startOaiHarvester
}