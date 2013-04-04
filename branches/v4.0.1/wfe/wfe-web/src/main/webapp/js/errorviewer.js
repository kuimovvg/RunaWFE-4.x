$(document).ready(function() {
	var buttons = {};
	buttons[buttonCloseMessage] = function() {
		$.errorDetailsDialog.dialog("close");
	};
	$.errorDetailsDialog = $("<div style=\"padding: 10px;\"><pre id=\"errorDetails\"></pre></div>").dialog( {
		modal: true, 
		autoOpen: false, 
		height: 500,
		width: 700,
		buttons: buttons,  
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		}  
	});
});

function showBotTaskConfigurationError(botId, botTaskName) {
	jQuery.ajax({
	    type: "POST",
	    url: "/wfe/error_details.do",
	    data: {
	    	action: "getBotTaskConfigurationError", 
	    	id: botId, 
	    	name: botTaskName
	    },
	    dataType: "html",
	    success: function(html) {
			$("#errorDetails").html(html);
			$.errorDetailsDialog.dialog("open");
	    }
    });
}

function showProcessError(processId, nodeId) {
	jQuery.ajax({
	    type: "POST",
	    url: "/wfe/error_details.do",
	    data: {
	    	action: "getProcessError", 
	    	id: processId, 
	    	name: nodeId
	    },
	    dataType: "html",
	    success: function(html) {
			$("#errorDetails").html(html);
			$.errorDetailsDialog.dialog("open");
	    }
    });
}
