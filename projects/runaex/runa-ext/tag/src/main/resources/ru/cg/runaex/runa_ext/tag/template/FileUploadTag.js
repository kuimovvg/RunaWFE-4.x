$(document).ready(function () {
  var field = $('#' +'${fieldId}');
  var form = field.closest('form');
  form.change(function () {
    form.valid();
  });
  form.keypress(function () {
    form.valid();
  });
  form.blur(function () {
    form.valid();
  });
  form.focus(function () {
    form.valid();
  });

  if (${signVerifyRequired}) {
    var formData = new FormData();

    formData.append('id', ${id});
    formData.append('schema', ${schema});
    formData.append('table', ${table});
    formData.append('field', '${fieldId}');
    formData.append('signColumnName', ${signColumnName});
    formData.append('processDefinitionId', getProcessDefinitionId());

    var $signDiv = $$("#${fieldId}-file_sign_div");
    var $msgDiv = $$("#${fieldId}-file_sign_div").children();

    $.ajax({
      url: 'fileSignVerify',
      type: 'POST',
      data: formData,
      success: function (response) {
        var iconName = 'signature-bad';
        if (response.passedVerify) {
          iconName = 'signature-ok'
        }
        $signDiv.prepend('<img src="resources/images/' + iconName + '.png"/>');
        $msgDiv.html(response.verifyMsg);
      },
      error: function () {
        $msgDiv.html(${errorDuringFileSignVerifyMsg});
      },
      cache: false,
      contentType: false,
      processData: false
    });
  }
});