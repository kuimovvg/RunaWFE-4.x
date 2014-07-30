
var componentInputUNIQUENAME = "COMPONENT_INPUT";
var lastIndexUNIQUENAME = -1;

$(document).ready(function() {
	lastIndexUNIQUENAME = parseInt(getSizeUNIQUENAME()) - 1;	
    $("#btnAddUNIQUENAME").click(function() {
        var rowIndex = parseInt(lastIndexUNIQUENAME) + 1;
        lastIndexUNIQUENAME = rowIndex;
		console.log("Adding row " + rowIndex);
        var e = "<div row='" + rowIndex + "' name='VARIABLE' style='margin-bottom:4px;'>";
        e += componentInputUNIQUENAME.replace(/\[\]/g, "[" + rowIndex + "]");
        e += "<input type='button' value=' - ' onclick='removeUNIQUENAME(this);' style='width: 30px;' />";
        e += "</div>";
        $("#btnAddUNIQUENAME").before(e);
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