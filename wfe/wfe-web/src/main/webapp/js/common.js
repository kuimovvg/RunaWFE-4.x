
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
	// http://docs.jquery.com/UI/Datepicker
	$(".inputDate").datepicker({ dateFormat: "dd.mm.yy", buttonImage: "/wfe/images/calendar.gif" });
	$(".inputDateTime").datetimepicker();
	// confirmation dialog
	$.confirmDialog = $("<div></div>").dialog({
		minWidth: 400, minHeight: 200, modal: true, autoOpen: false
	});
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

function openConfirmPopup(element, cookieName, message, confirmMessage, cancelButton, okButton) {
	if($.cookie(cookieName) == "true") {
		if(element.href == null) {
			var parent = element.parentNode;
			while(parent.tagName != "FORM") {
				parent = parent.parentNode; 
			}
			parent.submit();
		} else { 
			window.location = element.href; 
		}
	} else {
		$.confirmDialog.html("<p>" + message + "</p><p><input id=\"cookieCh\" type=\"checkbox\" value=\"\"> " + confirmMessage + "</p>"); 
		var buttons = {};
		buttons[okButton] = function() {
			if($("#cookieCh").is(":checked")) { 
				$.cookie(cookieName, "true");
			}
			var parent = element.parentNode;
			while(parent.tagName != "FORM") {
				parent = parent.parentNode; 
			}
			if (element.href == null) { 
				parent.submit(); 
			} else { 
				window.location = element.href; 
			}
		}
		buttons[cancelButton] = function() {
			$(this).dialog("close");
		};
		$.confirmDialog.dialog("option", "buttons", buttons);
		$.confirmDialog.dialog("option", "position", "center");
		$.confirmDialog.dialog("open");
	}
}

function openSubstitutionCriteriasConfirmPopup(formId, message, allMethod, allButton, onlyMethod, onlyButton, cancelButton) {
	$.confirmDialog.html("<p>" + message + "</p>"); 
	var form = $("#formId");
	var buttons = {};
	buttons[onlyButton] = function() {
		form.removeMethod.value = onlyMethod;
		form.submit();
	};
	buttons[allButton] = function() {
		form.removeMethod.value = allMethod;
		form.submit();
	};
	buttons[cancelButton] = function() {
		$(this).dialog("close");
	};
	$.confirmDialog.dialog("option", "buttons", buttons);
	$.confirmDialog.dialog("open");
}