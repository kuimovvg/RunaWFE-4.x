$.fn.bootstraptable = function (options) {

    var ajaxLoading = false;

    var defOptions = {
        url: 'unknown.htm',
        columns: [
            {header: 'column1', width: '10%', html: '<div>${column1}</div>'},
            {header: 'column2', width: '30%', html: '<div>${column2}</div>'},
            {header: 'column3', width: '60%', html: '<div>${column3}</div>'}
        ],
        currentPage: 0,
        paginationSize: 20,
        height: '300px',
        loadingGif: 'none',
        onRowClick: null,
        emptyValue: 'Список пуст',
        usePagination: true,
        filter: {},
        order: {},
        visibleColumns: []
    };
    options = $.extend(defOptions, options);
    var $this = $(this);

    var $loadRow = createLoadRow(options);

    var $thead = $(createTableHeader(options));
    $this.append($thead);

    var $div = $('<div></div>');

    var $table = $("<table class='table table-bordered-bottom'></table>");
    var $tbody = $('<tbody></tbody>');
    $table.append($tbody);
    $div.append($table);

    $(this).append($div);

    if (options.usePagination) {
        $div.scroll(function (event) {
            if ($div[0].scrollHeight - $div.scrollTop() == $div.outerHeight()) {
                options.currentPage++;
                loadData($tbody, options);
            }
        });
    }
    loadData($tbody, options);

    function blockUI() {
        $tbody.append($loadRow);
    }

    function unblockUI() {
        $loadRow.remove();
    }

    function getRow(object, options) {
        var html = "<tr>";
        var columnNumber = 0;
        for (var i in options.columns) {
            var column = options.columns[i];
            var replacedHtml = column.html;
            var columnNamesArray = column.html.match(RegExp("{[^}]*}", 'ig'));
            var isVisible = true;
            if (options.visibleColumns[columnNumber] == false) {
                isVisible = false;

            }
            if (columnNamesArray != null) {
                for (var i = 0; i < columnNamesArray.length; i++) {
                    var columnName = columnNamesArray[i].replace('\{', '').replace('\}', '');
                    replacedHtml = replacedHtml.replace('{' + columnName + '}', object[columnName] ? object[columnName] : '');
                }
                html += '<td style="' + (!isVisible ? 'display:none' : '') + '" column-number="' + columnNumber + '" width="' + column.width + '">' + replacedHtml + '</td>';
            } else {
                html += '<td style="' + (!isVisible ? 'display:none' : '') + '" column-number="' + columnNumber + '" width="' + column.width + '">' + replacedHtml + '</td>';
            }
            columnNumber++;
        }
        html += '</tr>';
        return html;
    }

    function loadData() {
        var data = {
            "start": options.currentPage * options.paginationSize,
            "count": options.paginationSize,
            "category": options.filter.category,
            "businessProcessName": options.filter.businessProcessName,
            "taskFilter": options.filter.taskFilter
        };

        var filterText = data.taskFilter;
        var businessProcessName = data.businessProcessName;
        var category = data.category;

        $.ajax({
            url: options.url,
            async: false,
            cache: false,
            data: "start="+data.start+"&count="+data.count+"&businessProcessName="+businessProcessName+"&category="+category+"&taskFilter="+filterText,
            beforeSend: function () {
                ajaxLoading = true;
                blockUI();
            },
            success: function (data, textStatus) {
                var objects = data;
                if (objects.length == 0 && options.currentPage == 0) {
                    $tbody.append(getEmptyRow(options));
                }
                for (var i in objects) {
                    var object = objects[i];
                    var $row = getRow(object, options);
                    $tbody.append($row);
                }

              if (businessProcessName == undefined && category == undefined) {
                    buildInbox(filterText);
              }
            },
            error: function (error) {

            },
            complete: function (data) {
                ajaxLoading = false;
                unblockUI();
            }

        });
    }

    function getEmptyRow(options) {
        return $('<tr class="emptyrow"><td style="text-align:left" colspan="' + options.columns.length + '">' + options.emptyValue + '</td></tr>');
    }

    function createLoadRow(options) {
        return $('<tr><td style="text-align:left" colspan="' + options.columns.length + '"><img src="' + options.loadingGif + '"/></td></tr>');
    }

    function createTableHeader(options) {
        var columnNumber = 0;
        var html = '<table class="table table-bordered-top"><thead><tr>';
        var isVisible = true;
        if (options.visibleColumns[columnNumber] == false) {
            isVisible = false;
        }
        for (var i = 0; i < options.columns.length; i++) {
            html += '<th column-number="' + columnNumber + '" style="text-align:left;' + (!isVisible ? 'display:none;' : '') + '" width="' + options.columns[i].width + '">' + options.columns[i].header + '</th>';
            columnNumber++;
        }
        html += '</tr></thead></table>';
        return html;
    }


    var table = {
        options: options
    };

    table.addFilterField = function ($field) {
        if ($field) {
            $field.keyup(function (event) {
                table.addFilter($field.attr('name'), $field.val());
                table.refresh();
            })
        }
    };


    table.addFilterField = function ($field, filterName) {
        if ($field) {
            $field.keyup(function (event) {
                table.addFilter(filterName, $field.val());
                table.refresh();
            })
        }
    }

    table.addFilter = function (key, value) {
        table.options.filter[key] = value;
    };

    table.getFilter = function (key, value) {
        return table.options.filter;
    };

    table.addOrder = function (column, ask) {
        table.options.order[column] = ask;
    };

    table.refresh = function () {
        $tbody.empty();
        options.currentPage = 0;
        loadData();
    }

    table.clearAllOrders = function () {
        table.options.order = {};
    }

    table.clearAllFilters = function () {
        table.options.filter = {};
    }

    table.clearInboxFilters = function () {
        table.options.filter.businessProcessName = "undefined";
        table.options.filter.category = "undefined";
    }

    table.setVisible = function (columnIndex, isVisible) {
        options.visibleColumns[columnIndex] = isVisible;
        if (isVisible) {
            $tbody.find('tr.td[column-number="' + columnIndex + '"]').css('display', '');
            $thead.find('th[column-number="' + columnIndex + '"]').css('display', '');
        } else {
            $tbody.find('tr.td[column-number="' + columnIndex + '"]').css('display', 'none');
            $thead.find('th[column-number="' + columnIndex + '"]').css('display', 'none');
        }
    }

    return table;
}

var TableOrders = {
    ASC: 'asc',
    DESC: 'desc'
}