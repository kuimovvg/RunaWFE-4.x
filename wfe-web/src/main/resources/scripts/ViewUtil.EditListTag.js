
var componentInputUNIQUENAME = "COMPONENT_INPUT";

$(document).ready(function() {
    $("#btnAddUNIQUENAME").click(function() {
        var rowIndex = getSizeUNIQUENAME();
		console.log("Adding row " + rowIndex);
        var e = "<div row='" + rowIndex + "' style='margin-bottom:4px;'>";
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
	$("div[row='"+oldIndex+"'] input").each(function() {
		updateIndexedNameUNIQUENAME($(this), oldIndex, newIndex);
	});
	$("div[row='"+oldIndex+"'] select").each(function() {
		updateIndexedNameUNIQUENAME($(this), oldIndex, newIndex);
	});
	$("div[row='"+oldIndex+"'] textarea").each(function() {
		updateIndexedNameUNIQUENAME($(this), oldIndex, newIndex);
	});
	$("div[row='"+oldIndex+"']").attr("row", newIndex);
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

