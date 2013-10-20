function extractFormData() {
  var valid = true;
  var currentClientDate = parseDate(new Date());
  var form = $('#runaex_form_validation_id');
  if (form.valid)
    valid = form.valid();
  if (!valid)
    return {valid: valid};

  var data = [];
  var formData = new FormData();
  var filesAndFileSignForDeletion = [];
//  Числовые поля
  //var formId = $(elem).attr("form-id");
  var formId = null;
  var fields;
  if (formId == null)
    fields = $("input.runaex[type=\"number\"][data-schema]");
  else
    fields = $("input.runaex[type=\"number\"][form-id=\"" + formId + "\"]");

  for (var i = 0; i < fields.length; i++) {
    if (!fields[i].hasAttribute('data-tableid') && $(fields[i]).closest('.control-group').hasClass('error')) {
      valid = false;
    }
    if(pushValue(fields[i])){
      var fieldData = {
      "schema": $(fields[i]).attr("data-schema"),
      "table": $(fields[i]).attr("data-table"),
      "column": $(fields[i]).attr("name"),
      "value": $(fields[i]).unmaskedValue(),
      "tableId": $(fields[i]).attr("data-tableId")
    };
    data.push(fieldData);
    }
  }

  //  Текстовые поля
  //var formId = $(elem).attr("form-id");
  if (formId == null) {
    fields = $("input.runaex[type=\"text\"][data-schema]");
  }
  else {
    fields = $("input.runaex[type=\"text\"][form-id=\"" + formId + "\"]");
  }
  for (i = 0; i < fields.length; i++) {
    if (!fields[i].hasAttribute('data-tableid') && $(fields[i]).closest('.control-group').hasClass('error')) {
      valid = false;
    }
    if(pushValue(fields[i])){
      var fieldData = {
      "schema": $(fields[i]).attr("data-schema"),
      "table": $(fields[i]).attr("data-table"),
      "column": $(fields[i]).attr("name"),
      "value": $(fields[i]).unmaskedValue(),
      "tableId": $(fields[i]).attr("data-tableId")
    };
    data.push(fieldData);
    }
  }

  //  Комбобоксы
  fields = [];
  if (formId == null) {
    fields = $("select.runaex[data-schema]");
  }
  else {
    fields = $("select.runaex[form-id=\"" + formId + "\"]");
  }
  for (i = 0; i < fields.length; i++) {
    if (!fields[i].hasAttribute('data-tableid') && $(fields[i]).closest('.control-group').hasClass('error')) {
      valid = false;
    }
    if(pushValue(fields[i])){
      var fieldData = {
      "schema": $(fields[i]).attr("data-schema"),
      "table": $(fields[i]).attr("data-table"),
      "column": $(fields[i]).attr("name"),
      "value": $(fields[i]).val(),
      "tableId": $(fields[i]).attr("data-tableId")
    };
    data.push(fieldData);
    }
  }

//    Чекбоксы
  fields = [];
  if (formId == null) {
    fields = $("input.runaex[type=\"checkbox\"][data-schema]");
  }
  else {
    fields = $("input.runaex[type=\"checkbox\"][form-id=\"" + formId + "\"]");
  }
  for (i = 0; i < fields.length; i++) {
    if (!fields[i].hasAttribute('data-tableid') && $(fields[i]).closest('.control-group').hasClass('error')) {
      valid = false;
    }
    if(pushValue(fields[i])){
      var fieldData = {
      "schema": $(fields[i]).attr("data-schema"),
      "table": $(fields[i]).attr("data-table"),
      "column": $(fields[i]).attr("name"),
      "value": $(fields[i]).prop('checked'),
      "tableId": $(fields[i]).attr("data-tableId")
    };
    data.push(fieldData);
    }
  }

  //  Радиобаттоны
  fields = [];
  if (formId == null) {
    fields = $("input.runaex[type=\"radio\"]:checked[data-schema]:not([value=\"null\"])");
  }
  else {
    fields = $("input.runaex[type=\"radio\"][form-id=\"" + formId + "\"]:checked");
  }
  for (i = 0; i < fields.length; i++) {
    if (!fields[i].hasAttribute('data-tableid') && $(fields[i]).closest('.control-group').hasClass('error')) {
      valid = false;
    }
    if(pushValue(fields[i])){
      var fieldData = {
      "schema": $(fields[i]).attr("data-schema"),
      "table": $(fields[i]).attr("data-table"),
      "column": $(fields[i]).attr("name"),
      "value": $(fields[i]).val(),
      "tableId": $(fields[i]).attr("data-tableId")
    };
    data.push(fieldData);
    }
  }

  //Autocomplete
  fields = [];
  if (formId == null) {
    fields = $("input.runaex.ac-hidden-input[type=\"hidden\"][data-schema]");
  }
  else {
    fields = $("input.runaex.ac-hidden-input[type=\"hidden\"][form-id=\"" + formId + "\"]");
  }
  for (i = 0; i < fields.length; i++) {
    if (!fields[i].hasAttribute('data-tableid') && $(fields[i]).closest('.control-group').hasClass('error')) {
      valid = false;
    }
    if (pushValue(fields[i])) {
      var fieldData = {
        "schema": $(fields[i]).attr("data-schema"),
        "table": $(fields[i]).attr("data-table"),
        "column": $(fields[i]).attr("name"),
        "value": $(fields[i]).val(),
        "tableId": $(fields[i]).attr("data-tableId")
      };
      data.push(fieldData);
    }
  }

  //Select Tree Grid
  fields = $("div.dynatree-validate");
  for (i = 0; i < fields.length; i++) {
    if ($(fields[i]).find('.dynatree-active').length > 0) {
      $(fields[i]).find('.treegrid-error').remove();
    }
    else {
      valid = false;
      $(fields[i]).append("<label class='error treegrid-error'>Это поле обязательно для заполнения.</label>");
    }
  }
  fields = $("span.dynatree-active");
  for (i = 0; i < fields.length; i++) {
    var dynatree = $("div").find("[save-schema][save-table][save-field][data-piid]");
    if (pushValue(fields[i])) {
      var fieldData = {
        "schema": dynatree.attr("save-schema"),
        "table": dynatree.attr("save-table"),
        "column": dynatree.attr("save-field"),
        "value": $(fields[i]).find('a').attr('id')
      };
      data.push(fieldData);
    }
  }

  //other hidden input
  var schema, table, dataTableId;
  fields = [];
  if (formId == null) {
    fields = $("input.runaex.save-hidden-input[type=\"hidden\"]");
  }
  else {
    fields = $("input.runaex.save-hidden-input[type=\"hidden\"][form-id=\"" + formId + "\"]");
  }
  var isCurrentClientDate = false;
  currentClientDate = parseDate(new Date());
  for (i = 0; i < fields.length; i++) {
    isCurrentClientDate = true;
    schema = $(fields[i]).attr("data-schema");
    table = $(fields[i]).attr("data-table");
    dataTableId = $(fields[i]).attr("data-tableId");
    var value = $(fields[i]).val();
    var autoGeneratePattern = null;
    var autoGenerateSequence = null;

    if ($(fields[i]).attr("current-time-as-default-value") == "false") {
      isCurrentClientDate = false;
    }
    else if ($(fields[i]).attr("generate-automatically") == "true") {
      autoGeneratePattern = $(fields[i]).attr("pattern");
      autoGenerateSequence = $(fields[i]).val();
      value = null;
    }
    if (pushValue(fields[i])) {
      var fieldData = {
        "schema": schema ? schema : '',
        "table": table ? table : '',
        "column": $(fields[i]).attr("name"),
        "value": value,
        "tableId": dataTableId ? dataTableId : '',
        "currentTimeDefaultValue": isCurrentClientDate ? currentClientDate : null,
        "currentUserAsDefaultValue": $(fields[i]).attr("current-user-as-default-value") == "true",
        "autoGeneratePattern": autoGeneratePattern,
        "autoGenerateSequence": autoGenerateSequence
      };
      data.push(fieldData);
    }
  }

  //TextAreas
  fields = [];
  if (formId == null) {
    fields = $("textarea.runaex");
  }
  else {
    fields = $("textarea.runaex[form-id=\"" + formId + "\"]");
  }
  for (i = 0; i < fields.length; i++) {
    if (pushValue(fields[i])) {
      var fieldData = {
        "schema": $(fields[i]).attr("data-schema"),
        "table": $(fields[i]).attr("data-table"),
        "column": $(fields[i]).attr("name"),
        "value": $(fields[i]).val(),
        "tableId": $(fields[i]).attr("data-tableId")
      };
      data.push(fieldData);
    }
  }

  // FileUpload fields
  fields = [];
  if (formId == null) {
    fields = $("input.runaex[type=\"file\"][data-schema]");
  }
  else {
    fields = $("input.runaex[type=\"file\"][form-id=\"" + formId + "\"]");
  }
  var fileKey;
  $.each(fields, function (index, field) {
//    We use double backslash because colons are ignored in fileKey
    fileKey = $(field).attr('data-schema') + '\\' + $(field).attr('data-table') + '\\' + $(field).attr('name') + '\\' + $(field).attr('data-tableId') + '\\' + $(field).attr('sign-column-name');
    if (field.files.length == 0 && $('.runaex.deletefile[id=\"delete-' + $(field).attr('id') + '\"]').prop('checked')) {
      filesAndFileSignForDeletion.push(fileKey);
    }
    else if (field.files.length == 0 && $(field).attr('is-default-file-from-db')) {
      if (pushValue(fields[i])) {
        var fieldData = {
          "schema": $(fields[i]).attr("data-schema"),
          "table": $(fields[i]).attr("data-table"),
          "column": $(fields[i]).attr("name"),
          "value": $(field).attr("file-name-value"),
          "defaultFileFromDb": true,
          "schemaReference": $(field).attr("data-schema-reference"),
          "tableReference": $(field).attr("data-table-reference"),
          "fieldReference": $(field).attr("data-field-reference")
        };
        data.push(fieldData);
      }
    }
    else {
      $.each(field.files, function (index, file) {
        formData.append(fileKey, file);
      })
    }
  });

  return {valid: valid, data: data, filesAndFileSignForDeletion: filesAndFileSignForDeletion, formData: formData, currentClientDate: currentClientDate};
}

function parseDate(inputDate) {
  var dateArray = inputDate.toString().split(' ');
  var year = dateArray[3];
  var day = dateArray[2];
  var timeOnly = dateArray[4].split(':');

  return day + "." + getMonth(inputDate.getMonth()) + "." + year + " " + timeOnly[0] + ":" + timeOnly[1] + ":" + timeOnly[2];
}

function pushValue(field) {
  var p;
  var parents = $(field).parents();
  for (p = 0; p < parents.length; p++) {
    if (parents[p].className == "displayNone") {
      return false;
    }
  }
  return true;
}

function getMonth(month) {
  return ('0' + (month + 1)).slice(-2);
}

function getValueOfParam(param) {
  var val = $("#".concat(param)).unmaskedValue();
  if (val == "") {
    val = "null"
  }
  return val;
}