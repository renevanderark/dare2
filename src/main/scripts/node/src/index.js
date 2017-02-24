import "./polyfills"
import React from "react";
import ReactDOM from "react-dom";
import router from "./router";
import { connectSocket } from "./bootstrap";
import Perf from "react-addons-perf";

Perf.start();
window.setTimeout(() => {
    Perf.stop();
    const measurements = Perf.getLastMeasurements();
    Perf.printInclusive(measurements);
}, 30000);

document.addEventListener("DOMContentLoaded", () =>
    connectSocket(() => ReactDOM.render(router, document.getElementById("app"))));
