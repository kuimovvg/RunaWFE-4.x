
$(document).ready(function() {
  $(document).ajaxError(function(e, xhr, settings, exception) {
    $("#ajaxErrorsDiv").text("Ошибка Ajax запроса (" + settings.url + "): " + xhr.status + " (" + exception + ")");
  });
  $(document).ajaxSend(function(event, request, settings) {
    $("#ajaxErrorsDiv").text("");
  });
  // http://jqueryui.com/tooltip/
  $(document).tooltip();
  // http://trentrichardson.com/examples/timepicker/
  $(".inputTime").timepicker({ ampm: false, seconds: false });
  // $.timepicker.setDefaults($.timepicker.regional['ru']);
  // http://docs.jquery.com/UI/Datepicker
  $(".inputDate").datepicker({ dateFormat: "dd.mm.yy", showOn: "both", buttonImage: "/wfe/images/calendar.gif" });
  // $.datepicker.setDefaults($.datepicker.regional['ru']);
});

// add timestamp to ajax queries
function unify(url) {
	if (url.indexOf("?") != -1) {
		return url + "&t="+(new Date().getTime());
	}
	return url + "?t="+(new Date().getTime());
}

function clearErrorMessages(form) {
	$(".errorFor").each(function(){
		$(this).remove();
	});
}

function addError(field, errorText) {
   	var errorImg = $("<img src='/wfe/images/error.gif' errorFor='yes' title='"+errorText+"'>");
	$(field).append(errorImg);
}
