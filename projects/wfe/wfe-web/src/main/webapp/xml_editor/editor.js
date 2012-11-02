var xmleditor = null;

function getDocument(document_url) {
	$('#documentArea').val('');
	jQuery.ajax({
	    type: 'POST',
	    url: document_url,
	    data: '',
	    dataType: 'text/xml',
	    success: function(msg) { $('#documentArea').val(msg); initEditor(); },
		error: function(error) {
	    	if(error.readyState == 4 && error.status == 200) {$('#documentArea').val(error.responseText); initEditor();}
	    	else if(error.readyState == 4 && error.status == 500) {alert('Internal server error');}
	    	}
    }); 
}

function saveDocument(save_url) {
	jQuery.ajax({
		type: 'POST',
		url: save_url,
		data: {conf : xmleditor.getCode() },
		dataType: 'text/xml',
		success: function(msg) {},
		error: function(error) {alert('Internal server error');}
	});
}

function openDocumentEditor(url, save_url, button_save, button_cancel) {
	$.editor = $('<div><textarea id="documentArea" style="width:98%; height:98%; display: none; "></textarea></div>').dialog( {
		modal:true, autoOpen: false, draggable: false, 
		overlay: {backgroundColor: '#000', opacity: 0.5},
	        close: function(event, ui){$(this).dialog('destroy').remove();destroyEditor();}  
	} );
	
	 var winH = $(window).height();
	 var winW = $(window).width();
	 var height = winH;// * 0.75;
	 var width = winW * 0.75;
	 
	$.editor.dialog('option', 'height', height);
	$.editor.dialog('option', 'width', width);
	var mybuttons = {};
	mybuttons[button_cancel] = function() {$(this).dialog('close'); $(this).dialog('destroy').remove();destroyEditor(); };
	mybuttons[button_save] = function() {saveDocument(save_url); $(this).dialog('close'); $(this).dialog('destroy').remove();destroyEditor();};
	$.editor.dialog('option', 'buttons', mybuttons);
	$.editor.dialog('open');
	
	getDocument(url);
}

function initEditor() {
	if(xmleditor == null) {
		xmleditor = CodeMirror.fromTextArea('documentArea', {
			content: $("#documentArea").val(),
		    parserfile: "parsexml.js",
		    stylesheet: "xml_editor/css/xmlcolors.css",
		    path: "xml_editor/js/",
		    continuousScanning: 500,
		    autoMatchParens: true,
		    reindentOnLoad: true,
		    lineNumbers: false
		  });
	}
}

function destroyEditor() {
	xmleditor = null;
}