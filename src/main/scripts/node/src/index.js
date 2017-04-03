import "./polyfills"
import React from "react";
import ReactDOM from "react-dom";
import router from "./router";
import { connectSocket } from "./bootstrap";
// import Perf from "react-addons-perf";


/*function runPerf() {
    Perf.start()
    window.setTimeout(() => {
        Perf.stop();
        const measurements = Perf.getLastMeasurements();
        Perf.printInclusive(measurements);
    }, 1000);
}

setInterval(runPerf, 1100);*/

connectSocket(() => ReactDOM.render(router, document.getElementById("app")));
