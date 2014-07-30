
var ellUNIQUENAMEVariableNames = [VARIABLE_NAMES];
var ellUNIQUENAMERowTemplate = "ROW_TEMPLATE";
var lastIndexUNIQUENAME = -1;

$(document).ready(function() {
	lastIndexUNIQUENAME = parseInt(ellUNIQUENAMEGetSize()) - 1;
    $('#ellUNIQUENAMEButtonAdd').click(function() {
        var rowIndex = parseInt(lastIndexUNIQUENAME) + 1;
	lastIndexUNIQUENAME = rowIndex;
		console.log("UNIQUENAME: Adding row " + rowIndex);
        var e = "<tr row='" + rowIndex + "'>";
        e += ellUNIQUENAMERowTemplate.replace(/\[\]/g, "[" + rowIndex + "]");
        e += "<td><input type='button' value=' - ' onclick='ellUNIQUENAMERemoveRow(this);' /></td>";
        e += "</tr>";
        $('#ellUNIQUENAME').append(e);
        ellUNIQUENAMEUpdateSize(1);
        JS_HANDLERS
        $("#ellUNIQUENAME").trigger("onRowAdded", [rowIndex]);
    });
});

function ellUNIQUENAMEGetSize() {
	return parseInt($("#ellUNIQUENAME").attr("rowsCount"));
}

function ellUNIQUENAMERemoveRow(button) {
	var tr = $(button).closest("tr");
	var rowIndex = parseInt(tr.attr("row"));
	console.log("UNIQUENAME: Removing row " + rowIndex);
	tr.find(".inputFileDelete").each(function() {
		$(this).click();
	});
	tr.remove();
	ellUNIQUENAMEUpdateSize(-1);
    $("#ellUNIQUENAME").trigger("onRowRemoved", [rowIndex]);
}

function ellUNIQUENAMEUpdateSize(delta) {
    for (i in ellUNIQUENAMEVariableNames) {
		var sizeInput = $("input[name='" + ellUNIQUENAMEVariableNames[i] + ".size']");
		sizeInput.val(parseInt(ellUNIQUENAMEGetSize()) + delta);
	}
	$("#ellUNIQUENAME").attr("rowsCount", ellUNIQUENAMEGetSize() + delta);
	console.log("UNIQUENAME: Lists size = " + ellUNIQUENAMEGetSize());
}
