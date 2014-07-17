
var componentInputUNIQUENAMEKey = "COMPONENT_INPUT_KEY";
var componentInputUNIQUENAMEValue = "COMPONENT_INPUT_VALUE";

$(document).ready(function() {
    $("#btnAddMapUNIQUENAME").click(function() {
        var rowIndex = getSizeUNIQUENAME();
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
	div.remove();
	for (var i = rowIndex; i < size - 1; i++) {
		updateRowIndexesUNIQUENAME(i + 1, i);
	}
	updateSizeUNIQUENAME(-1);
    $("#UNIQUENAME").trigger("onRowRemoved", [rowIndex]);
}

function removeAllUNIQUENAME() {
	$("#UNIQUENAME div[row]").each(function() {
		$(this).remove();
	});
	$("input[name='VARIABLE.size']").val("0");
    $("#UNIQUENAME").trigger("onAllRowsRemoved");
	console.log("Removed all rows");
}

function updateRowIndexesUNIQUENAME(oldIndex, newIndex) {
	$("input[name='VARIABLE["+oldIndex+"]']").each(function() {
		updateIndexedNameUNIQUENAME($(this), oldIndex, newIndex);
	});
	$("select[name='VARIABLE["+oldIndex+"]']").each(function() {
		updateIndexedNameUNIQUENAME($(this), oldIndex, newIndex);
	});
	$("textarea[name='VARIABLE["+oldIndex+"]']").each(function() {
		updateIndexedNameUNIQUENAME($(this), oldIndex, newIndex);
	});
	$("div[row='"+oldIndex+"'][name='VARIABLE']").attr("row", newIndex);
}

function updateIndexedNameUNIQUENAME(element, oldIndex, newIndex) {
	var name = element.attr("name");
	if (name == null) {
		console.log("name is null in ", element);
		return;
	}
	name = name.replace("[" + oldIndex + "]", "[" + newIndex + "]");
	element.attr("name", name);
}

function updateSizeUNIQUENAME(delta) {
	var sizeInput = $("input[name='VARIABLE.size']");
	sizeInput.val(parseInt(sizeInput.val()) + delta);
	console.log("List size = " + getSizeUNIQUENAME());
}

