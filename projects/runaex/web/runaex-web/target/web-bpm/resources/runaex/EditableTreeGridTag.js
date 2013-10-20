function onRowClickHandler(src) {
  var $clickedRow = $(src);
  var isSelected = $clickedRow.hasClass("trSelected");

  var rowId = "";
  var flexigrid = $clickedRow.closest('table[id^=\"flexigrid\"]');
  var schema = flexigrid.attr('data-schema');
  var table = flexigrid.attr('data-table');
  var link = flexigrid.attr('data-link');
  var baseObject = flexigrid.attr('data-baseObject');
  var piid = flexigrid.attr('data-piid');
  var tableId = flexigrid.attr('data-tableId');

  if (isSelected) {
    rowId = $clickedRow.prop('id').replace('row', '');
  }
  var dataForSend = {"piid": piid, "rowId": rowId, "schema": schema, "table": table, "link": link, "baseObject": baseObject};
  saveOrEnqueueSelectedRow(dataForSend, tableId);
}

function doSmallHead() {
  var editableTreePanelGrid = $('#editableTreePanelGrid');
  var xBoxInner = editableTreePanelGrid.find('.x-grid-header-ct .x-box-inner');
  var xColumnHeaderInner = editableTreePanelGrid.find('.x-grid-header-ct .x-box-inner .x-column-header-inner');
  var height = xBoxInner.height();
  var prevHeight = height;
  var editableTreePanelGridBody = $('#editableTreePanelGrid-body');
  var isOk = true;
  $.each(xColumnHeaderInner, function (index, value) {
    if (isOk)
      if (parseInt($(value).css('padding-top')) == 0) {
        isOk = false;
      }
  });
  if (!isOk) {
    return;
  }
  height *= 0.6;
  xBoxInner.css('height', height + 'px');
  xBoxInner.children().css('height', height + 'px');
  xColumnHeaderInner.css('height', height + 'px');
  xColumnHeaderInner.children()
  xColumnHeaderInner.css('padding-top', height * 0.4 + 'px');
  $.each(xColumnHeaderInner.children('span'), function (index, value) {
    var paddingTop = (height - $(value).height()) / 2;
    if (paddingTop < 0)
      paddingTop = 0;
    $(value).parent().css('padding-top', paddingTop + 'px')
  });
  editableTreePanelGridBody.css('top', (parseInt(editableTreePanelGridBody.css('top')) - prevHeight + height) + 'px');
}