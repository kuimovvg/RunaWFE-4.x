$('#' + ${fieldId} +'-speech').bind('webkitspeechchange', function () {
  var val = this.value;
  $($('#' + ${fieldId}).children().attr('selected', null).filter(function () {
    return this.label.toLowerCase().indexOf(val.toLowerCase()) == 0;
  }).get(0)).attr('selected', '');
});