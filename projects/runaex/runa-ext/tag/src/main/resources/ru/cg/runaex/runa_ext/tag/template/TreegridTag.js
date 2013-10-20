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
    onActivate: function (node) {
      var data = {
        piid: ${processInstanceId},
        schema: ${schema},
        table:  ${table},
        rowId: node.data.key,
        rowParentId: node.data.parentId
      };
      onSelectTreeItemClickHandler(data);
    },
    onCustomRender: function (node) {
      // Render title as columns
      if (node.data.title.indexOf("~") === -1) {
        return false;
      }
      var cols = node.data.title.split("~")
      html = "<a class='dynatree-title' href='#'>";
      for (var i = 0; i < cols.length; i++) {
        html += "<span class='td'>" + cols[i] + "</span>";
      }
      return html + "</a>";
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
  });
  $("#" +${btnReloadActive}).click(function () {
    var node = $("#" +${table}).dynatree("getActiveNode");
    if (node && node.isLazy()) {
      node.reloadChildren(function (node, isOk) {
      });
    }
  })
});