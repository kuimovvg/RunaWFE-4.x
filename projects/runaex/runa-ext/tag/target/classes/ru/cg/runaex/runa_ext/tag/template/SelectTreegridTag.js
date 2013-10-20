$(function () {
// --- Initialize sample trees
  $('#' +${table}).dynatree({
    fx: { height: 'toggle', duration: 200 },
    autoFocus: false,
    initAjax: {
      url: 'treegrid',
      data: {
        schema: ${schema},
        table:  ${table},
        fields: ${fields},
        processDefinitionId: getProcessDefinitionId()
      }
    },
//    onActivate: function (node) {
//      var div = node.tree.tnRoot.ul.parentNode;
//      var rowId = node.li.getElementsByClassName("dynatree-title")[0].id;
//      var schema = $(div).attr('save-schema');
//      var table = $(div).attr('save-table');
//      var field = $(div).attr('save-field');
//      var piid = $(div).attr('data-piid');
//      var dataForSend = {"piid": piid, "rowId": rowId, "schema": schema, "table": table, "field": field};
//      $.ajax({
//          type: "POST",
//          url: 'saveSelectedRow',
//          data: dataForSend
//      });
//    },
    onCustomRender: function (node) {
      // Render title as columns
      if (node.data.title != null && node.data.title.indexOf("~") === -1) {
        return false;
      }
      if (node.data.title != null) {
        var cols = node.data.title.split("~");
        html = "<a class='dynatree-title' href='#' id=" + node.data.key + ">";
        for (var i = 0; i < cols.length; i++) {
          html += "<span class='td'>" + cols[i] + "</span>";
        }
        return html + "</a>";
      }
    },
    onLazyRead: function (node) {
      node.appendAjax({
        url: "treegrid",
        data: {
          schema: ${schema},
          table:  ${table},
          fields: ${fields},
          processDefinitionId: getProcessDefinitionId(),
          parentId: node.data.key
        }
      });
    }
  })
});