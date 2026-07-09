// document.addEventListener("DOMContentLoaded", function () {
//
//     const subcontractorTable = document.getElementById("subcontractors-table");
//
//     if (subcontractorTable) {
//
//         const sortableTable = new MOJFrontend.SortableTable({
//             table: subcontractorTable
//         });
//
//         sortableTable.init();
//     }
//
// });




// document.addEventListener("DOMContentLoaded", function () {
//     var table = document.getElementById("subcontractors-table");
//
//     if (!table) {
//         return;
//     }
//
//     var sortUrl = table.getAttribute("data-sort-url");
//
//     if (!sortUrl) {
//         return;
//     }
//
//     function getCurrentSortOrder(header) {
//         return header.getAttribute("aria-sort") || "none";
//     }
//
//     function getNextSortOrder(currentSortOrder) {
//         if (currentSortOrder === "ascending") {
//             return "descending";
//         }
//
//         return "ascending";
//     }
//
//     function redirectToSortedPage(column, nextSortOrder) {
//         var currentParams = new URLSearchParams(window.location.search);
//
//         currentParams.set("sortBy", column);
//         currentParams.set("sortOrder", nextSortOrder);
//
//         // Always reset to page 1 when sorting.
//         currentParams.delete("page");
//
//         window.location.href = sortUrl + "?" + currentParams.toString();
//     }
//
//     document.addEventListener(
//         "click",
//         function (event) {
//             var header = event.target.closest("th[data-sort-column]");
//
//             if (!header || !table.contains(header)) {
//                 return;
//             }
//
//             event.preventDefault();
//             event.stopPropagation();
//
//             var column = header.getAttribute("data-sort-column");
//             var currentSortOrder = getCurrentSortOrder(header);
//             var nextSortOrder = getNextSortOrder(currentSortOrder);
//
//             redirectToSortedPage(column, nextSortOrder);
//         },
//         true
//     );
// });






// document.addEventListener("DOMContentLoaded", function () {
//     if (window.MOJFrontend) {
//         window.MOJFrontend.initAll();
//     }
// });





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