var ie6compatibility = $.browser.msie && $.browser.version < 8;

$(function() {	
	
	getVariableInfo($("#variableSelect").val());
	
	$("#variableSelect").bind("change", function() {
		getVariableInfo($("#variableSelect").val());		
	});
	 
});

function getVariableInfo(value) {
	jQuery.ajax({
		type: "GET",
		cache: false,
		url: "/wfe/getComponentInput",
		data: {
			processId: decodeURI((RegExp('id=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]),
			variableName: value
		},
		dataType: "html",
		success: function (e, msg, result) { 
			var data = jQuery.parseJSON(result.responseText);
			
			$("#variableScriptingInfo").empty();
			$("#variableCurrentInfo").empty();
			$("#variableInput").empty();
			
			var scriptingValue = "<div class=\"variableLabel\">" + data.scriptingName + "</div>";
			$("#variableScriptingInfo").append(scriptingValue);
			
			var currentValue = "";
			if(data.variableIsNull == "true") {
				currentValue = "<div class=\"variableLabel\"><input type='checkbox' disabled='true' checked>NULL</div>";
				$("#nullValueCheckbox").css("display", "none");
				$("#nullValueLabel").css("display", "none");
			} else {
				currentValue = "<div class=\"variableScript\">" + data.variableValue + "</div>";
				$("#nullValueCheckbox").css("display", "inline");
				$("#nullValueLabel").css("display", "inline");
			}						
			$("#variableCurrentInfo").append(currentValue); 
			
			$("#variableInput").append(data.input);
			
			$('#nullValueCheckbox').change(function() {
		        if($(this).is(':checked')) {
		        	$("[name=" + $("#variableSelect").val() + "]").attr('disabled', 'true');
		        } else {
		        	$("[name=" + $("#variableSelect").val() + "]").attr('disabled', null);
		        }
		    });
			
			$(".inputTime").timepicker({ ampm: false, seconds: false });
			$(".inputDate").datepicker({ dateFormat: "dd.mm.yy", buttonImage: "/wfe/images/calendar.gif" });
			$(".inputDateTime").datetimepicker({ dateFormat: "dd.mm.yy" });
			
			$(".inputFileAttachButtonDiv").css("display", "none");
		}
	});	
	
}
