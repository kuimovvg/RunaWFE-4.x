var labels =${labels};
var onFocusOutHandler = function () {
  if (!$(this).val()) {
    removeSphinxSearchFields(${fieldId});
  }
};
$('#' + ${fieldId} +'.ac-input').focusout(onFocusOutHandler);
var changeHandler = function (event, ui) {
  if (ui.item) {
    var idValues = ui.item.ids;
    var objects = ui.item.objects;
    createSphinxSearchFields(${fieldId}, objects, idValues, labels);
  }
  else {
    removeSphinxSearchFields(${fieldId});
  }
};
$('#' + ${fieldId} +'.ac-input').autocomplete({
  minLength: ${varMinSymbols},
  delay:${varQueryDelay},
  source: function (request, response) {
    $.ajax({url: 'sphinxSearch', type: 'POST', dataType: 'text',
      data: {query: request.term,
        indexName: ${indexName}
      },
      complete: function (resp) {
        if (resp.status == 200)
          response($.parseJSON(resp.responseText))
      }
    });
  },
  select: changeHandler,
  change: changeHandler,
  open: function () {
    $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
  },
  close: function () {
    $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
  }
});

//Если открыли для редактирования то надо подгрузить данные
if (${editingMode}) {
  var ids =${ids};
  var objects =${objects};
  createSphinxSearchFields(${fieldId}, objects, ids, labels);
}