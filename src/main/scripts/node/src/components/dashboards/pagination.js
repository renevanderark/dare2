import React from "react";

const getPageRange = (currentPage, pagesAround, amountOfPages) => {
    const min = currentPage - pagesAround;
    const max = currentPage + pagesAround;
    const adjustMin = min < 1 ? -(min) + 1 : 0;
    const adjustMax = max > amountOfPages ? amountOfPages - max : 0;
    const firstPage = min + adjustMin + adjustMax < 1 ? 1 : min + adjustMin + adjustMax;
    const lastPage = max + adjustMin + adjustMax;


    let result = [];
    for (let i = firstPage; i <= amountOfPages && i <= lastPage; i++) {
        result.push(i);
    }

    return result;
};

const Pagination = ({offset, limit, count, onPageClick}) => {
    const amountOfPages = parseInt(Math.floor(count / limit), 10) + ((count % limit === 0) ? 0 : 1);
    const currentPage = parseInt(Math.floor((offset + limit) / limit), 10);

    const pageRange = getPageRange(currentPage, 2, amountOfPages);

    return (
        <div className="text-center">
            <ul className="pagination">
                <li className={currentPage === 1 ? "disabled" : ""}>
                    <a onClick={() => onPageClick(0)}>
                        <span>&laquo;</span>
                    </a>
                </li>
                {pageRange.map((page) => (
                    <li key={page} className={currentPage === page ? "active" : ""}>
                        <a onClick={() => onPageClick((page - 1) * limit)}>{page}</a>
                    </li>
                ))}
                <li className={currentPage === amountOfPages ? "disabled" : ""}>
                    <a onClick={() => onPageClick((amountOfPages - 1) * limit) }>
                        <span>&raquo;</span>
                    </a>
                </li>
            </ul>
        </div>
    );
};

Pagination.propTypes = {
    offset: React.PropTypes.number.isRequired,
    limit: React.PropTypes.number.isRequired,
    count: React.PropTypes.number.isRequired,
    onPageClick: React.PropTypes.func.isRequired
};

export default Pagination;