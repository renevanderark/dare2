const numberFormat = (num) =>
    `${num}`.replace(/./g, (c, i, a) =>
        i && ((a.length - i) % 3 === 0) ? '.' + c : c);

export { numberFormat };