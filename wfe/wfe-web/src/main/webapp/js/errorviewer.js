$(document).ready(function() {
	var buttons = {};
	buttons["Support"] = function() {
		showSupportFiles();
	};
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
	$.ajax({
		dataType: "json",
	    url: "/wfe/error_details.do",
	    data: {
	    	action: "getBotTaskConfigurationError", 
	    	id: botId, 
	    	name: botTaskName
	    },
	    success: function(data) {
			$("#errorDetails").html(data.html);
			$.errorDetailsDialog.dialog("open");
	    }
    });
}

function showProcessError(processId, nodeId) {
	$.ajax({
		dataType: "json",
	    url: "/wfe/error_details.do",
	    data: {
	    	action: "getProcessError", 
	    	id: processId, 
	    	name: nodeId
	    },
	    success: function(data) {
			$("#errorDetails").html(data.html);
			$.errorDetailsDialog.dialog("open");
	    }
    });
}

function showSupportFiles() {
	$("#errorDetails").html("<img href='/wfe/images/loading.gif' align='absmiddle' /> " + loadingMessage);
	$.errorDetailsDialog.dialog("open");
    $.getJSON(
		"/wfe/error_details.do?action=showSupportFiles&" + $("#supportForm").serialize(),
		function(data) {
			$("#errorDetails").html("");
			$.each(data.includedFiles, function(i, item) {
				$("#errorDetails").append("<div><input type='radio' disabled='true' checked='"+item.included+"'>"+item.info+"</a></div>");
			});
			$("#errorDetails").append("<br /><br /><a href='" + data.downloadUrl + "'>" + data.downloadTitle + "</a>");
		}
	);
}
