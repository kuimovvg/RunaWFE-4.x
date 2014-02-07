
var ellUNIQUENAMEVariableNames = [VARIABLE_NAMES];
var ellUNIQUENAMERowTemplate = "ROW_TEMPLATE";

$(document).ready(function() {
    $('#ellUNIQUENAMEButtonAdd').click(function() {
        var rowIndex = ellUNIQUENAMEGetSize();
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
	var size = ellUNIQUENAMEGetSize();
	console.log("UNIQUENAME: Removing row " + rowIndex);
	tr.remove();
	for (var i = rowIndex; i < size - 1; i++) {
		ellUNIQUENAMEUpdateRowIndexes(i + 1, i);
	}
	ellUNIQUENAMEUpdateSize(-1);
    $("#ellUNIQUENAME").trigger("onRowRemoved", [rowIndex]);
}

function ellUNIQUENAMEUpdateRowIndexes(oldIndex, newIndex) {
	$("tr[row='"+oldIndex+"'] input").each(function() {
		ellUNIQUENAMEUpdateIndexedName($(this), oldIndex, newIndex);
	});
	$("tr[row='"+oldIndex+"'] select").each(function() {
		ellUNIQUENAMEUpdateIndexedName($(this), oldIndex, newIndex);
	});
	$("tr[row='"+oldIndex+"'] textarea").each(function() {
		ellUNIQUENAMEUpdateIndexedName($(this), oldIndex, newIndex);
	});
	$("tr[row='"+oldIndex+"']").attr("row", newIndex);
}

function ellUNIQUENAMEUpdateIndexedName(element, oldIndex, newIndex) {
	var name = element.attr("name");
	if (name == null) {
		console.log("UNIQUENAME: Name is null in ", element);
		return;
	}
	name = name.replace("[" + oldIndex + "]", "[" + newIndex + "]");
	element.attr("name", name);
}

function ellUNIQUENAMEUpdateSize(delta) {
    for (i in ellUNIQUENAMEVariableNames) {
		var sizeInput = $("input[name='" + ellUNIQUENAMEVariableNames[i] + ".size']");
		sizeInput.val(parseInt(sizeInput.val()) + delta);
	}
	$("#ellUNIQUENAME").attr("rowsCount", ellUNIQUENAMEGetSize() + delta);
	console.log("UNIQUENAME: Lists size = " + ellUNIQUENAMEGetSize());
}
