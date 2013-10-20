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