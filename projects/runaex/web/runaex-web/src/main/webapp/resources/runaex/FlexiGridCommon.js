resized = false;
CONST_MIN_GRID_HEIGHT = 200;
CONST_MIN_GRID_WIDTH = 160;
CONST_MIN_GRID_WIDTH_USAGE_PAGINATION = 500;
CONST_FLEXI_GRID_HEIGHT = 70;

var tableCallQueues = {};

function saveOrEnqueueSelectedRow(dataForSend, tableId) {
  var data = {"dataForSend": dataForSend, "tableId": tableId};
  var callQueue = tableCallQueues[tableId];

  if (callQueue == undefined) {
    callQueue = [];
    tableCallQueues[tableId] = callQueue;
  }

  callQueue.push(data);

  if (callQueue.length == 1) {
    saveSelected(data);
  }
}

function saveSelected(data) {
  var tableId = data['tableId'];
  var dataForSend = data['dataForSend'];
  var linkedButtons = $('button[type=\"button\"][data-tableId=\"' + tableId + '\"]');
  linkedButtons.attr('disabled', '');
  $.ajax({
    type: "POST",
    url: 'saveSelectedRow',
    data: dataForSend,
    complete: function (xhr) {
      var callQueue = tableCallQueues[tableId];
      var sentData = callQueue.shift();
      var selectedRow = sentData.dataForSend.rowId;
      if (callQueue.length > 0) {
        var nextCallData = callQueue.pop();
        tableCallQueues[tableId] = [nextCallData];
        saveSelected(nextCallData);
      }
      else {
        if (xhr.status == 200 && selectedRow) {
          linkedButtons.removeAttr('disabled');
        }
      }
    }
  });
}

function resizeContainer() {
//  if (resized) {
//    return;
//  }
//  resized = true;
//  var section = $('.section');
//  section.css('height',
//      $('body').height()
//          - $('.navbar').height() - $('.reserved').height()
//          - parseInt(section.css('padding-top')) - parseInt(section.css('padding-bottom')) //TODO convert em to px
//          - parseInt(section.css('margin-top')) - parseInt(section.css('margin-bottom'))
//          - 1 //rounding error
//  );
//  $('#form').css('height', section.height() + parseInt(section.css('padding-top')) + parseInt(section.css('padding-bottom')));
}

function getTotalHeight() {
  var section = $('.otherPages');
  var sum = $('body').outerHeight(true);
  var total = $(document).height() - $('.navbar').height() - $('.footer').height()
      - parseInt(section.css('padding-top')) - parseInt(section.css('padding-bottom'))
      - parseInt(section.css('margin-top')) - parseInt(section.css('margin-bottom')) - 1;
  return  $(document).height() - sum;
}

function computeFreeWidth(gridWidthWeight, flexiGridId, usagePagination) {
  var usageTable = false;
  var gridElement = $('#' + flexiGridId);
  if (gridElement.parent('td').length)
    usageTable = true;

  var form = $('#form');
  var totalWidth = form.width() - 10;

  var sum = 0;
  var minWidth = 10000;

  var w;

  $('.flexigrid-mark').each(function (i, el) {
    var widthWeight = parseInt($(el).attr('width-weight'));
    if (minWidth > widthWeight) {
      minWidth = widthWeight;
    }
  });

  if (usageTable) {
    var indexes = new Array();
    gridElement.parents('tr:eq(0)').find('.flexigrid-mark').each(function (i, el) {
      indexes[indexes.length] = $(el).parents('td:eq(0)').index();
      var widthWeight = parseInt($(el).attr('width-weight'));
      sum += parseFloat(widthWeight);
    });

    var usageWidth = 0;
    gridElement.parents('tr:eq(0)').children().each(function (i, el) {
      if ($.inArray(i, indexes) == -1) {
        usageWidth += $(el).outerWidth(true);
      }
      else {
        usageWidth += parseInt($(el).css('padding-left').replace("px", "")) + parseInt($(el).css('padding-right').replace("px", ""));
      }
    });
    var columnCount = indexes.length;
    totalWidth -= usageWidth;

    var tempTotalWidth = sum * CONST_MIN_GRID_WIDTH_USAGE_PAGINATION / ( minWidth);

    if (tempTotalWidth <= totalWidth) {
      w = (totalWidth * gridWidthWeight) / (100 * columnCount);
    }
    else {
      w = (CONST_MIN_GRID_WIDTH_USAGE_PAGINATION * gridWidthWeight) / (minWidth);
    }
  }
  else {
    w = totalWidth * gridWidthWeight / 100;
  }

  if (usagePagination && w < CONST_MIN_GRID_WIDTH_USAGE_PAGINATION) {
    w = CONST_MIN_GRID_WIDTH_USAGE_PAGINATION;
  }
  else if (!usagePagination && w < CONST_MIN_GRID_WIDTH) {
    w = CONST_MIN_GRID_WIDTH;
  }
  return w;
}


function computeFreeHeight(gridHeightWeight, flexiGridId, isFlexGid) {
  var usageTable = false;
  var gridElement = $('#' + flexiGridId);
  if (gridElement.parent('td').length)
    usageTable = true;

  var sum = 0;
  var minHeight = 10000;

  var totalHeight = getTotalHeight();
  if (isFlexGid) {
    totalHeight -= CONST_FLEXI_GRID_HEIGHT;
  }
  var gridCount = 0;
  if (usageTable) {
    var index = gridElement.parents('td').index();

    //Выбираем все tr

    gridElement.parents('tbody').children('tr').each(function (elementIndex, element) {
      $(element).children('td:eq(' + index + ')').find('.flexigrid-mark').each(function (elIndex, el) {
        var heightWeight = parseInt($(el).attr('height-weight'));
        if (minHeight > heightWeight) {
          minHeight = heightWeight;
        }
        sum += parseFloat(heightWeight);
        gridCount++;
      });
    });
  }
  else {
    $('.flexigrid-mark').each(function (index, element) {
      var heightWeight = parseFloat($(element).attr('height-weight'));
      if (minHeight > heightWeight) {
        minHeight = heightWeight;
      }
      sum += heightWeight;
    });
  }

  var h = gridHeightWeight * CONST_MIN_GRID_HEIGHT / ( minHeight);
  var tempTotalHeight = sum * CONST_MIN_GRID_HEIGHT / ( minHeight);

  if (tempTotalHeight > totalHeight) {
    totalHeight = tempTotalHeight;
  }
  else {
    if (usageTable) {
      h = (totalHeight * gridHeightWeight) / (100 * gridCount);
    }
    else {
      h = (totalHeight * gridHeightWeight) / (100);
    }
  }

  if (h < CONST_MIN_GRID_HEIGHT) {
    return CONST_MIN_GRID_HEIGHT;
  }

  return h;
}

function flexiGridFilterKeyListener(event, gridElementId) {
  $('#flexigrid-' + gridElementId).flexFind(gridElementId);
}