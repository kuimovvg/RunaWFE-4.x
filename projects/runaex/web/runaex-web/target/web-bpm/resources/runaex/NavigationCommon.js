function nbtAfterGroovyHandler(elem) {
  elem.disabled = true;
  var onclick = elem.attributes.getNamedItem("onclick");
  elem.attributes.removeNamedItem("onclick");
  var nextTask = $(elem).attr("next-task");
  var action = $(elem).attr("action");
  var taskId = document.getElementsByName("id")[0].value;
  var taskName = document.getElementsByName("taskName")[0].value;
  var processInstanceId = document.getElementsByName("processInstanceId")[0].value;
  var processName = document.getElementsByName("processName")[0].value;
  var url = 'navigate';
  blockMainUI();
  $.ajax({
    type: "POST",
    url: url,
    data: { "nextTask": nextTask, "processInstanceId": processInstanceId, "taskId": taskId, "taskName": taskName, "action": action, "processName": processName },
    success: function (response) {
      if (response.id == -1)
        location.href = "tasks";
      else
        location.href = "task?id=" + response.id + "&name=" + response.name + "&pname=" + response.pname;
    },
    error: function () {
      elem.disabled = false;
      elem.attributes.setNamedItem(onclick);
    },
    complete: function () {
      unblockMainUI();
    },
    dataType: 'json'
  });
}
function nbtClickHandler(elem) {
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
      success: function () {
        nbtAfterGroovyHandler(elem);
      },
      error: function () {
        unblockMainUI();
        elem.disabled = false;
      },
      cache: false,
      contentType: false,
      processData: false
    });
  }
  else {
    nbtAfterGroovyHandler(elem);
  }
}