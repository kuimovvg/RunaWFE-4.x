$(document).ready(function() {
	var buttons = {};
	buttons[buttonSupportMessage] = function() {
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
	var url = "/wfe/error_details.do?action=showSupportFiles&" + $("#supportForm").serialize();
	$("#errorDetails").html("<br/><br/><br/>&nbsp;&nbsp;&nbsp;<img src='/wfe/images/loading.gif' align='absmiddle' /> " + loadingMessage);
	$.errorDetailsDialog.dialog("open");
    $.getJSON(
		url,
		function(data) {
			$("#errorDetails").html("<div id='processTabs'><ul id='processHeaders'></ul></div>");
  			$.each(data.processesErrorInfo, function(i, processErrorInfo) {
  				$("#processHeaders").append("<li><a href='#processError" + processErrorInfo.id + "'>" + processErrorInfo.id + "</a></li>");
				$("#processTabs").append("<div id='processError" + processErrorInfo.id + "'></div>");
				$.each(processErrorInfo.includedFileNames, function(i, fileInfo) {
					var presentationFileInfo = "<input type='checkbox' disabled='true'";
					if (fileInfo.included) {
						presentationFileInfo += " checked='true'";
					}
					presentationFileInfo += ">" + fileInfo.info + "<br />";
					$("#processError" + processErrorInfo.id).append(presentationFileInfo);
				});
			});
			$("#processTabs").tabs();
			if (data.downloadUrl) {
				$("#errorDetails").append("<br /><br /><a href='" + data.downloadUrl + "' style='text-decoration: underline;'>" + data.downloadTitle + "</a>");
			}
		}
	);
}
