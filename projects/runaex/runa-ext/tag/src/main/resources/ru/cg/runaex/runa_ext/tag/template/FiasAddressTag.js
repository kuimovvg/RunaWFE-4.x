if (!${usageActual}) {
  var fullField = $(document.getElementById(${fieldId}));
  var dateField = $(document.getElementById(${dateFieldId}));
  var guidColumn = $(document.getElementById(${guidColumn}));
  var changeDateHandler = function () {
    var fullField = $(document.getElementById(${fieldId}));
    var dateField = $(document.getElementById(${dateFieldId}));
    var guidColumn = $(document.getElementById(${guidColumn}));
    if (dateField.val() != '') {
      fullField.removeAttr('readonly');
      guidColumn.val('');
      fullField.val('');
      guidColumn.valid();
    }
    else {
      fullField.attr('readonly', '');
      guidColumn.val('');
      fullField.val('');
      guidColumn.valid();
    }
  };
  var focusOutHandler = function () {
    if(fullField.val() == "" && guidColumn.val() != "") {
        guidColumn.val(null);
        guidColumn.valid();
    }
  }
  dateField.bind('change', changeDateHandler);
  fullField.bind('focusout', focusOutHandler);
}



var changeHandler = function (event, ui) {
  if (ui.item) {
    var selectedItemId = ui.item.id;
    $('input[type="hidden"][name="' + ${guildColumnWithGap} +'"]').val(selectedItemId);
  }
  else {
    $('input[type="hidden"][name="' + ${guildColumnWithGap} +'"]').val(null);
    $(this).val(null);
  }
  $('#' + ${guildColumnWithGap} +'[type="hidden"]').valid();
};
$('[id="' + ${fieldId} +'"].ac-input').autocomplete({
  minLength: ${minSymbols},
  delay:${queryDelay},
  source: function (request, response) {
    $.ajax({
      url: 'searchAddress?minLevel=' + ${fiasMinLevel} +"&maxLevel=" +${fiasMaxLevel},
      type: 'POST',
      dataType: 'jsonp',
      data: {
        query: request.term,
        defaultFilter: ${defaultFilter},
        historical: !${usageActual},
        historicDate: $(document.getElementById(${dateFieldId})).val()
      },
      complete: function (resp) {
        if (resp.status == 200) {
          response($.parseJSON(resp.responseText));
        }
      },
      errorMsgHandler: function (msg) {
        var labelTag = $(document.getElementById(${guidColumn})).siblings('label.error[for="' + ${guildColumnWithGap} +'"]').not('.valid');
        if (labelTag) {
          labelTag.text(msg);
          labelTag.closest('.control-group').addClass('error');
        }
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