$(document).ready(function () {
});

function blockMainUI(msg) {
  if (msg == null || msg == undefined)
    msg = '';
  $.blockUI({ message: '<div><img src="resources/images/ajax-loader.gif"/> <div style="color:#fff; font-size:20px;">' + msg + '</div></div>', css: { backgroundColor: 'transparent', border: 'none'} });
}

function unblockMainUI() {
  $.unblockUI();
}

$$ = function (value) {
  value = value.replace('#', '');
  return $(document.getElementById(value));
}

$.fn.unmaskedValue = function () {
  if ($(this).attr('mask-type') === "MONETARY_EXTENDED") {
    return $(this).val().split(' ').join('').replace('\,', '.');
  }
  else if ($(this).attr('mask-type') === "MONETARY") {
    return $(this).val().split(' ').join('');
  }
  else if ($(this).attr('mask-type') === "NUMBER") {
    return $(this).val();
  }
  else if ($(this).data('mask')) {
    return $(this).data('mask').valWithoutMask();
  }
  return $(this).val();
}

function isNumeric(value) {
  return !isNaN(new Number(value));
}

function getProcessDefinitionId() {
  return document.getElementById("processDefinitionId").value;
}
function encodeToUtf8(s) {
  return unescape(encodeURIComponent(s));
}