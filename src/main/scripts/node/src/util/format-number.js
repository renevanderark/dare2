const numberFormat = (num) =>
    `${num}`.replace(/./g, (c, i, a) =>
        i && ((a.length - i) % 3 === 0) ? '.' + c : c);

const toHumanFileSize = (bytes) => {
    if (bytes === 1) { return "?"}
    const thresh = 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' B';
    }
    const units = ['kB','MB','GB','TB','PB','EB','ZB','YB'];
    let u = -1;
    do {
        bytes /= thresh;
        ++u;
    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
    return bytes.toFixed(1) + units[u];
};

export { numberFormat, toHumanFileSize };