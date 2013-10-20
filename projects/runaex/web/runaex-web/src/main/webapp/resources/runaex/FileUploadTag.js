function downloadFile(schema, table, field, id) {
  $.download('downloadfile', {schema: schema, table: table, field: field, id: id, processDefinitionId: getProcessDefinitionId()});
}

function clearFileUploadInput(field) {
  $$('#' + field).val(null).change();
}

$(document).ready(function() {
  $('.runaex.fileupload').change(function(eventObject) {
    var target = $(eventObject.target);
    var val = target.val();
    var id = target.attr('id');
    var field = $$('#'+id+'-hidden');
    if (val && val.trim() != '') {
      $('.runaex.clearfileupload[id=\"clear-' + target.attr('id') + '\"]').attr('disabled', null);
      field.attr('value',val);
    }
    else {
      $('.runaex.clearfileupload[id=\"clear-' + target.attr('id') + '\"]').attr('disabled', '');
      if (field.attr('uploaded') == null)
        field.attr('value', '');
    }
  });

  $('.runaex.deletefile').change(function(eventObject) {
    var target = $(eventObject.target);
    var val = target.prop('checked');
    var fileInputId = target.attr('name').replace('delete-', '');
    if (val) {
      $('.runaex.fileupload[id=\"' + fileInputId + '\"]').val(null).change().attr('disabled', '');
    }
    else {
      $('.runaex.fileupload[id=\"' + fileInputId + '\"]').val(null).change().attr('disabled', null);
    }
  });

  $('.runaex.downloadfile').tooltip();
});