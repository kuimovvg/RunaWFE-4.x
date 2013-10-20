function print(jsComp) {
  var button = null;
  var tableId = null;
  var templateFileName = null;
  var storedProcedures = null;
  var reportType = null;
  var data = null;
  var proceduresWithFilledParams = null;

  if (jsComp.tagName == 'A') {
    button = $(jsComp).closest('ul').siblings('button');
    tableId = button.attr('tableId');
    templateFileName = button.attr('template_file_name');
    storedProcedures = button.attr('stored_procedures');
    if (storedProcedures != undefined) {
      var procedures = storedProcedures.split(";");
      proceduresWithFilledParams = '';
      for (var j in procedures) {
        var procedure = procedures[j];
        var procedureName = procedure.substring(0, procedure.indexOf("(")).trim();
        var beginIndex = procedure.indexOf("(");
        var endIndex = procedure.indexOf(")");
        var parametersStr = procedure.substring(beginIndex + 1, endIndex);
        var parameters = [];
        if (parametersStr.trim() != "") {
          parameters = parametersStr.split(",");
        }
        if (procedure != "") {
          proceduresWithFilledParams = proceduresWithFilledParams.concat(procedureName, "(");
          if (parameters.length > 0) {
            for (var k in parameters) {
              var param = parameters[k].trim();
              var val = getValueOfParam(param);
              proceduresWithFilledParams = proceduresWithFilledParams.concat(val, ", ");
            }
            proceduresWithFilledParams = proceduresWithFilledParams.substring(0, proceduresWithFilledParams.length - 2);
          }
          proceduresWithFilledParams = proceduresWithFilledParams.concat(");");
        }
      }
    }
    reportType = $(jsComp).attr('report_type');
  }
  else {
    tableId = $(jsComp).attr('tableId');
  }

  if (tableId != null && tableId != '') {
    var grid = $('.flexigrid-mark[data-tableid=\"' + tableId + '\"]');
    if (grid.length > 0) {
      data = 'columns=';
      var columns = grid.getVisibleColumns();
      if(!columns)
        columns = grid.getTreeGridVisibleColumns();
      for (var i = 0; i < columns.length; ++i) {
        var column = columns[i];
        if (i > 0) {
          data += ',';
        }
        data += column.dbName + '::' + column.displayName;
      }

      var object1 = grid.attr('data-object1'),
          object2 = grid.attr('data-object2');

      data += '&schema=' + grid.attr('data-schema') + '&table=' + grid.attr('data-table') + '&tableId=' + grid.attr('data-tableid')
          + '&sortname=' + grid.options('sortname') + '&sortorder=' + grid.options('sortorder')
          + '&object1=' + (object1 ? object1 : '') + '&object2=' + (object2 ? object2 : '') + '&processDefinitionId=' + getProcessDefinitionId();
    }
  }
  else {
    var formDataObj = {};
    var fields;
    fields = $("input.runaex[type=\"number\"][data-schema]");
    for (var i = 0; i < fields.length; i++) {
      formDataObj[$(fields[i]).attr("id")] = $(fields[i]).val();
    }
    //  Текстовые поля
    fields = $("input.runaex[type=\"text\"]:not(.ac-input)");
    for (i = 0; i < fields.length; i++) {
      formDataObj[$(fields[i]).attr("id")] = $(fields[i]).val();
    }

    //  Комбобоксы
    fields = $("select.runaex[data-schema]");
    for (i = 0; i < fields.length; i++) {
      var $field = $(fields[i]);
      formDataObj[$field.attr("id")] = $field.find('option:selected').attr('label');
    }

    //  Чекбоксы
    fields = $("input.runaex[type=\"checkbox\"][data-schema]");
    for (i = 0; i < fields.length; i++) {
      formDataObj[$(fields[i]).attr("id")] = $(fields[i]).prop('checked');
    }

    //  Радиобаттоны
    fields = $("input.runaex[type=\"radio\"]:checked[data-schema]");
    for (i = 0; i < fields.length; i++) {
      var visibleLabel = $(fields[i]).siblings('label');
      formDataObj[$(fields[i]).attr("id")] = visibleLabel.html().trim();
    }

    //Autocomplete
    fields = $("input.runaex.ac-hidden-input[type=\"hidden\"][data-schema]");

    for (i = 0; i < fields.length; i++) {
      var visibleInput = $(fields[i]).siblings('input');
      formDataObj[$(fields[i]).attr("id")] = visibleInput.val();
    }

    //TextAreas
    fields = $("textarea.runaex");
    for (i = 0; i < fields.length; i++) {
      formDataObj[$(fields[i]).attr("id")] = $(fields[i]).val();
    }

    //Labels
    fields = $("label.lt-label");
    for (i = 0; i < fields.length; i++) {
      formDataObj[$(fields[i]).attr("id")] = $(fields[i]).html();
    }

    var formData = JSON.stringify(formDataObj);
    data = "formData=" + formData;
  }

  if (data != null) {
    if (templateFileName != null) {
      data += "&templateFileName=" + templateFileName;
    }
    if (reportType != null) {
      data += "&reportType=" + reportType;
    }

    if (proceduresWithFilledParams != null) {
      data += "&storedProcedures=" + proceduresWithFilledParams;
    }

    var url = 'print';
    $.download(url, data);
  }
}