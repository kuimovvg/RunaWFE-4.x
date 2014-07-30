
var componentInputUNIQUENAMEKey = "COMPONENT_INPUT_KEY";
var componentInputUNIQUENAMEValue = "COMPONENT_INPUT_VALUE";
var lastIndexUNIQUENAME = -1;

$(document).ready(function() {
	lastIndexUNIQUENAME = parseInt(getSizeUNIQUENAME()) - 1;
    $("#btnAddMapUNIQUENAME").click(function() {
    	var rowIndex = parseInt(lastIndexUNIQUENAME) + 1;
    	lastIndexUNIQUENAME = rowIndex;
		console.log("Adding row " + rowIndex);
        var e = "<div row='" + rowIndex + "' name='VARIABLE' style='margin-bottom:4px;'>";
        e += componentInputUNIQUENAMEKey.replace(/\[\]/g, "[" + rowIndex + "]");
        e += componentInputUNIQUENAMEValue.replace(/\[\]/g, "[" + rowIndex + "]");
        e += "<input type='button' value=' - ' onclick='removeUNIQUENAME(this);' style='width: 30px; margin-top: 6px;' />";
        e += "</div>";
        $("#btnAddMapUNIQUENAME").before(e);
        updateSizeUNIQUENAME(1);
        COMPONENT_JS_HANDLER
        $("#UNIQUENAME").trigger("onRowAdded", [rowIndex]);
    });
});

function getSizeUNIQUENAME() {
	return parseInt($("input[name='VARIABLE.size']").val());
}

function removeUNIQUENAME(button) {
	var div = $(button).closest("div");
	var rowIndex = parseInt(div.attr("row"));
	var size = getSizeUNIQUENAME();
	console.log("Removing row ", rowIndex);
	div.find(".inputFileDelete").each(function() {
		$(this).click();
	});
	div.remove();
	updateSizeUNIQUENAME(-1);
    $("#UNIQUENAME").trigger("onRowRemoved", [rowIndex]);
}

function removeAllUNIQUENAME() {
	$("#UNIQUENAME div[row]").each(function() {
		$(this).find(".inputFileDelete").each(function() {
			$(this).click();
		});
		$(this).remove();
	});
	$("input[name='VARIABLE.size']").val("0");
    $("#UNIQUENAME").trigger("onAllRowsRemoved");
	console.log("Removed all rows");
}

function updateSizeUNIQUENAME(delta) {
	var sizeInput = $("input[name='VARIABLE.size']");
	sizeInput.val(parseInt(sizeInput.val()) + delta);
	console.log("List size = " + getSizeUNIQUENAME());
}