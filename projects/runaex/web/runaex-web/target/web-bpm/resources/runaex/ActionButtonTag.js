function sbtClickHandler(elem) {
  elem.disabled = true;
  var scriptId = $(elem).attr("script_id");

  blockMainUI();

  if (scriptId != undefined) {
    var formData = getExtractedData(elem);

    if (formData == undefined) {
      unblockMainUI();
      return;
    }

    formData.append('script_id', scriptId);

    $.ajax({
      url: 'handleGroovyScriptButton',
      type: 'POST',
      data: formData,
      success: function () {
        sbtActionAfterGroovyHandler(elem, formData);
      },
      complete: function () {
        unblockMainUI();
      },
      error: function () {
        elem.disabled = false;
      },
      cache: false,
      contentType: false,
      processData: false
    });
  }
  else {
    sbtActionAfterGroovyHandler(elem);
  }
}

function getExtractedData(elem) {
  var formData = new FormData();
  var action = $(elem).attr("action");
  var extractedData;
  var valid = true;
  var data = JSON.stringify([]);
  var filesAndFileSignForDeletion = [];
  var taskId = document.getElementsByName("id")[0].value;

  if (action == 'save' || action == 'find' || action == 'saveandlink') {
    extractedData = extractFormData();
  }

  if (extractedData) {
    valid = extractedData.valid;
    data = JSON.stringify(extractedData.data);
    formData = extractedData.formData;
    filesAndFileSignForDeletion = extractedData.filesAndFileSignForDeletion;
  }

  if (!valid) {
    elem.disabled = false;
    return;
  }

  formData.append('filesAndFileSignForDeletion', filesAndFileSignForDeletion);
  formData.append('data', data);
  formData.append('action', action);
  formData.append('taskId', taskId);
  return formData;
}

function sbtActionAfterGroovyHandler(elem, formDataParam) {
  elem.disabled = true;
  var nextTask = $(elem).attr("next-task");
  var formData = '';

  if (formDataParam == undefined)
    formData = getExtractedData(elem);
  else
    formData = formDataParam;

  if (formData == undefined) {
    unblockMainUI();
    return;
  }

  var taskName = document.getElementsByName("taskName")[0].value;
  var processInstanceId = document.getElementsByName("processInstanceId")[0].value;
  var processName = document.getElementsByName("processName")[0].value;

  formData.append('processInstanceId', processInstanceId);
  formData.append('processName', processName);
  formData.append('taskName', taskName);
  formData.append('nextTask', nextTask);

  $.ajax({
    url: 'handleActionButton',
    type: 'POST',
    data: formData,
    success: function (response) {
      if (!response.noTasksAvailableInProcess) {
        var success = '';
        if (response.success)
          success = "&autohide=" + response.success;
        location.href = "task?id=" + response.id + "&name=" + response.name + "&pname=" + response.pname + success;
      }
      else
        location.href = "tasks";
    },
    complete: function () {
      unblockMainUI();
    },
    error: function () {
      elem.disabled = false;
    },
    cache: false,
    contentType: false,
    processData: false
  });

//  var oReq = new XMLHttpRequest();
//  oReq.open('POST', 'handleActionButton');
//  oReq.setRequestHeader("Content-Type", "multipart/form-data; charset=UTF-8");
//  oReq.addEventListener('load', function (response) {
//    if (!response.noTasksAvailableInProcess) {
//      var success = '';
//      if (response.success)
//        success = "&autohide=" + response.success;
//      location.href = "task?id=" + response.id + "&name=" + response.name + "&pname=" + response.pname + success;
//    }
//    else
//      location.href = "tasks";
//  });
//  oReq.addEventListener('error', function () {
//    elem.disabled = false;
//  });
//  oReq.addEventListener('loadend', function () {
//    unblockMainUI();
//  });
//  oReq.addEventListener('abort', function () {
//    unblockMainUI();
//  });
//  oReq.send(formData);
}