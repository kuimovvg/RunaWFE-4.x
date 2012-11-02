
$(document).ready(function() {
  $(document).ajaxError(function(event, request, settings) {
    $("#ajaxErrorsDiv").text("Ошибка Ajax запроса");
  });
  $(document).ajaxSend(function(event, request, settings) {
    $("#ajaxErrorsDiv").text("");
  });
});

// add timestamp to ajax queries
function unify(url) {
	if (url.indexOf("?") != -1) {
		return url + "&t="+(new Date().getTime());
	}
	return url + "?t="+(new Date().getTime());
}

// tooltip functions
function showTip(e, id) {
  var elem = document.getElementById(id);
  if (document.all) {
  	elem.style.left = window.event.x+document.body.scrollLeft;
    elem.style.top = window.event.y+document.body.scrollTop;
  } else {
    elem.style.left = e.pageX;
    elem.style.top = e.pageY;
  }
  elem.style.display = 'block';
}
 
function hideTip(id) {
  var elem = document.getElementById(id);
  elem.style.display = 'none';
}
