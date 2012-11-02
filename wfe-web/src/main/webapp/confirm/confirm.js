jQuery().ready(function($){
	$.confirmDialog = $('<div></div>').dialog( {
		width: 400,  height: 200, minWidth: 400, minHeight: 200,   modal:true, autoOpen: false, 
		overlay: {backgroundColor: '#000', opacity: 0.5}
	} );
});

function openConfirmPopup(mybutton, cookieName, message, message_confirm,  button_cancel, button_ok) {
	if($.cookie(cookieName) == 'true') {
		if(mybutton.href == null) {
			var parent = mybutton.parentNode;
			while(parent.tagName != 'FORM') {
				parent = parent.parentNode; 
			}
			parent.submit();
		}
		else { window.location = mybutton.href; }
	} else {
		$.confirmDialog.html('<p>' + message + '</p><p><input id=\"cookieCh\" type=\"checkbox\" value=\"\">'	+ message_confirm + '</p>'); 
		var mybuttons = {};
		mybuttons[button_cancel] = function() {$(this).dialog('close');};
		mybuttons[button_ok] = function() {
			if($('#cookieCh').is(':checked')) { 
				$.cookie(cookieName, 'true');
			}
			var parent = mybutton.parentNode;
			while(parent.tagName != 'FORM') {
				parent = parent.parentNode; 
			}
			if(mybutton.href == null) { parent.submit(); }
			else { window.location = mybutton.href; }
		}

		$.confirmDialog.dialog('option', 'buttons', mybuttons);
		$.confirmDialog.dialog('open');
	}
}

function openSubstitutionCriteriasConfirmPopup(form_id, message, method_all, button_all, method_only, button_only, button_cancel) {
	$.confirmDialog.html('<p>' + message + '</p>'); 
	var mybuttons = {};
	mybuttons[button_cancel] = function() {$(this).dialog('close');};
	mybuttons[button_only] = function() {
		var form = document.getElementById(form_id);
		form.removeMethod.value = method_only;
		form.submit();
	};
	mybuttons[button_all] = function() {
		var form = document.getElementById(form_id);
		form.removeMethod.value = method_all;
		form.submit();
	};

	$.confirmDialog.dialog('option', 'buttons', mybuttons);
	$.confirmDialog.dialog('open');
}