function onLinkTableSelectRowClickHandler(src) {
  var $clickedRow = $(src);
  var isSelected = $clickedRow.hasClass("trSelected");

  var rowId = "";
  var flexigrid = $clickedRow.closest('table[id^=\"flexigrid\"]');
  var schema = flexigrid.attr('data-schema');
  var table = flexigrid.attr('data-table');
  var baseObject = flexigrid.attr('data-object1');
  var object2 = flexigrid.attr('data-object2');
  var piid = flexigrid.attr('data-piid');
  var tableId = flexigrid.attr('data-tableid');

  if (isSelected) {
    rowId = $clickedRow.prop('id').replace('row', '');
  }

  var dataForSend = {"piid": piid, "rowId": rowId, "schema": schema, "table": table, "baseObject": baseObject, "object2": object2};
  saveOrEnqueueSelectedRow(dataForSend, tableId);
}