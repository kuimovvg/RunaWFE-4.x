$(document).ajaxError(function handleAjaxError(e, request, settings, exception) {

  var message = ajaxErrorAction(request);
  if (settings.errorMsgHandler) {
    settings.errorMsgHandler(message);
  }
  else {
    alertError(message);
  }
});

function ajaxErrorAction(request) {
  var message;
  var sessionExpired = request.status == 200 && request.responseText != null && request.responseText.indexOf("<html>") > -1;
  if (sessionExpired) {
    window.location.reload();
  }
  else {
    var statusErrorMap = {
      '400': "Ошибка синтаксиса запроса.",
      '401': "Необходима авторизация.",
      '403': "Доступ к ресурсу запрещён.",
      '500': "Внутренняя ошибка сервера: " + request.responseText,
      '503': "Сервис недоступен.",
      '420': request.responseText
    };
    if (request.status) {
      message = statusErrorMap[request.status];
      if (!message) {
        message = "Произошла неизвестная ошибка.";
      }
    }
    else if (e == 'parsererror') {
      message = "Не удалось разобрать запрос.";
    }
    else if (e == 'timeout') {
      message = "Тайм-аут запроса."
    }
    else if (e == 'abort') {
      message = "Запрос был прерван пользователем.";
    }
    else {
      message = "Произошла неизвестная ошибка.";
    }
    return message;
  }
}

function alertError(message) {
  $("#ajax-error").removeClass("hidden").text(message);
}