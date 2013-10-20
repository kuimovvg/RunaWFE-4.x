Ext.require([
  'Ext.data.*',
  'Ext.grid.*',
  'Ext.tree.*'
]);

Ext.onReady(function () {
  //for money formatting cells
  Ext.util.Format.thousandSeparator = ' ';
  Ext.util.Format.decimalSeparator = ',';

  Ext.QuickTips.init();

  //we want to setup a model and store instead of using dataUrl
  Ext.define('Task', {
    extend: 'Ext.data.Model',
    fields: ${modelFields}

  });

  Ext.override(Ext.data.AbstractStore, {
    indexOf: Ext.emptyFn
  });

  var gridWidth = Math.floor(computeFreeWidth(${widthWeight}, ${gridElementId}, ${isPaginationVisible}));
  var gridHeight = Math.floor(computeFreeHeight(${heightWeight}, ${gridElementId}, false));

  var store = Ext.create('Ext.data.TreeStore', {
    model: 'Task',
    proxy: {
      type: 'ajax',
      url: 'editableTreeGrid?s=' + ${schema} +'&parentCol=' + ${parentCol} +'&t=' + ${table} +'&dependent=' + ${isDependent} +'&processDefinitionId=' + getProcessDefinitionId() + '&fields=' + ${fields} +'&tableId=' + ${tableId} +'&linkColumns=' + ${linkColumns},
      listeners: {
        exception: function (proxy, response, options) {
          alertError(ajaxErrorAction(response));
        }
      }
    },
    autoLoad: true,
    folderSort: true,
    sorters: [
      {
        property: ${sortname},
        direction: ${sortorder}
      }
    ]
  });

  //Ext.ux.tree.TreeGrid is no longer a Ux. You can simply use a tree.TreePanel
  var grid = Ext.create('Ext.tree.Panel', {
    id: 'editableTreePanelGrid',
    title: '',
    width: gridWidth,
    height: gridHeight,
    renderTo: ${gridElementId},
    collapsible: true,
    useArrows: true,
    rootVisible: false,
    viewConfig: {
      toggleOnDblClick: false
    },

    store: store,
    multiSelect: true,
    columns:${columns},

    plugins: [
      Ext.create('Ext.grid.plugin.CellEditing', {
        clicksToEdit: 2
      })
    ]
  });

  function renderCellStyle(fieldNameCSS, formatCol) {
    return function render(value, metadata, record) {
      if (record.raw[fieldNameCSS] != null) {
        metadata.tdCls = record.raw[fieldNameCSS]
      }
      switch (formatCol) {
        case 0:
          return value;
        case 1:
          return Ext.util.Format.number(value, '0,000.00');
      }
      return value;
    }
  }

  var nodeCount = ${nodeCount};
  store.on('load', function (records, operation, success) {
    store.getRootNode().cascadeBy(function (node) {
      if (node.getDepth() < nodeCount) {
        if (!node.isExpanded() && !node.isLoaded())
          node.expand();
      }
      if (node.getDepth() == nodeCount) {
        return false;
      }
    });
  });

  grid.on('beforeedit', function (e) {
    var res = '';
    Ext.Ajax.request({
      url: 'checkTreeCellEdit?column=' + e.field + '&rowId=' + e.record.internalId,
      async: false,
      success: function (result, request) {
        res = result.responseText;
      },
      failure: function (response) {
        alertError(ajaxErrorAction(response));
      }
    });
    if (res == "false")
      return false;
  });

  grid.on('edit', function (editor, e) {
    var res = '';
    var store = e.grid.getStore();
    Ext.Ajax.request({
      url: 'updateTreeCellValue?s=' + ${schema} +'&parentCol=' + ${parentCol} +'&t=' + ${table} +'&dependent=' + ${isDependent} +'&fields=' + ${fields} +'&linkColumns=' + ${linkColumns} +'&column=' + e.field + '&rowId=' + e.record.internalId + '&value=' + e.value + '&originalValue=' + e.originalValue + '&processDefinitionId=' + getProcessDefinitionId() + '&tableId=' + ${tableId},
      async: false,
      success: function (result, request) {
        if (result.responseText) {
          var changeList = JSON.parse(result.responseText);
          for (var i = 0; i < changeList.length; i++) {
            var cellValue = changeList[i];
            var value = cellValue.value;
            var node = store.getNodeById(cellValue.cell.id);
            if (node) {
              node.set(cellValue.cell.column, value);
            }
          }
        }
        res = result.responseText;
      }, failure: function (response) {
        alertError(ajaxErrorAction(response));
      }
    });
  });

  $.fn.getTreeGridVisibleColumns = function () {
    var columns = new Array();
    this.each(function () {
      for (var i = 0; i < grid.columns.length; i++) {
        var column = grid.columns[i];
        columns.push({dbName: column.dataIndex.split(':')[2], displayName: column.text});
      }
    });
    return columns;
  };

  $.fn.getSortName = function () {
    for (var i = 0; i < grid.columns.length; i++) {
      if (grid.columns[i].id == 'orderNumberColumn') {
        return grid.columns[i].dataIndex.split(':')[2]
      }
    }
    return '';
  };

  $.fn.getSortOrder = function () {
    for (var i = 0; i < grid.columns.length; i++) {
      if (grid.columns[i].id == 'orderNumberColumn') {
        return grid.columns[i].sortState;
      }
    }
    return '';
  };

  $.fn.options = function (name) {
    if (name === 'sortname')
      return this.getSortName();
    else if (name === 'sortorder')
      return this.getSortOrder();
    return null;
  };

  doSmallHead();

  $('#editableTreePanelGrid .x-column-header').resize(function () {
    doSmallHead();
  });
});

