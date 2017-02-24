import "./polyfills"
import React from "react";
import ReactDOM from "react-dom";
import router from "./router";
import { connectSocket } from "./bootstrap";
/*import Perf from "react-addons-perf";


function runPerf() {
    window.setTimeout(Perf.start, 2000);
    window.setTimeout(() => {
        Perf.stop();
        const measurements = Perf.getLastMeasurements();
        Perf.printInclusive(measurements);
    }, 3000);
}

setInterval(runPerf, 5500);*/

document.addEventListener("DOMContentLoaded", () =>
    connectSocket(() => ReactDOM.render(router, document.getElementById("app"))));
