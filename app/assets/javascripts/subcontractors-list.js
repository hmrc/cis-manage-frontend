document.addEventListener("DOMContentLoaded", function () {
    if (typeof MOJFrontend !== "undefined") {
        MOJFrontend.initAll();
    }

    var table = document.getElementById("subcontractors-table");

    if (!table) {
        return;
    }

    var sortUrl = table.getAttribute("data-sort-url");

    if (!sortUrl) {
        return;
    }

    function getNextSortOrder(currentSortOrder) {
        if (currentSortOrder === "ascending") {
            return "descending";
        }

        return "ascending";
    }

    function redirectToServerSort(column, nextSortOrder) {
        var params = new URLSearchParams(window.location.search);

        params.set("sortBy", column);
        params.set("sortOrder", nextSortOrder);

        window.location.href = sortUrl + "?" + params.toString();
    }

    table.addEventListener(
        "click",
        function (event) {
            var sortableHeader = event.target.closest("th[data-sort-column]");

            if (!sortableHeader || !table.contains(sortableHeader)) {
                return;
            }

            event.preventDefault();
            event.stopImmediatePropagation();

            var column = sortableHeader.getAttribute("data-sort-column");
            var currentSortOrder = sortableHeader.getAttribute("aria-sort") || "none";
            var nextSortOrder = getNextSortOrder(currentSortOrder);

            redirectToServerSort(column, nextSortOrder);
        },
        true
    );
});