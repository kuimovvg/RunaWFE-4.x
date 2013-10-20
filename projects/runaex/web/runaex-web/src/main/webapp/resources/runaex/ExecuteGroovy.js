function groovyScriptBtnClickHandler(elem) {
  elem.disabled = true;
  var scriptId = $(elem).attr("script_id");
  if (scriptId != undefined) {
    var taskId = document.getElementsByName("id")[0].value;

    var formData = new FormData();
    formData.append('script_id', scriptId);
    formData.append('taskId', taskId);

    blockMainUI();
    $.ajax({
      url: 'handleGroovyScriptButton',
      type: 'POST',
      data: formData,
      complete: function () {
        unblockMainUI();
        elem.disabled = false;
      },
      error: function () {
        elem.disabled = false;
      },
      cache: false,
      contentType: false,
      processData: false
    });
  }
}