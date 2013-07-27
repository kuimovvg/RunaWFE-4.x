
var componentInputVARIABLE = "COMPONENT_INPUT";

$(document).ready(function() {
    $('#btnAddVARIABLE').click(function() {
        var rowIndex = getSizeVARIABLE();
		console.log("Adding row " + rowIndex);
        var e = "<div row='" + rowIndex + "' style='margin-bottom:4px;'>";
        e += componentInputVARIABLE.replace(/\[\]/g, "[" + rowIndex + "]");
        e += "<input type='button' value=' - ' onclick='removeVARIABLE(this);' />";
        e += "</div>";
        $('#btnAddVARIABLE').before(e);
        updateSizeVARIABLE(1);
        COMPONENT_JS_HANDLER
    });
});

function getSizeVARIABLE() {
	return parseInt($("input[name='VARIABLE.size']").val());
}

function removeVARIABLE(button) {
	var div = $(button).closest("div");
	var rowIndex = parseInt(div.attr("row"));
	var size = getSizeVARIABLE();
	console.log("Removing row ", rowIndex);
	div.remove();
	for (var i = rowIndex; i < size - 1; i++) {
		updateRowIndexesVARIABLE(i + 1, i);
	}
	updateSizeVARIABLE(-1);
}

function updateRowIndexesVARIABLE(oldIndex, newIndex) {
	$("div[row='"+oldIndex+"'] input").each(function() {
		updateIndexedNameVARIABLE($(this), oldIndex, newIndex);
	});
	$("div[row='"+oldIndex+"'] select").each(function() {
		updateIndexedNameVARIABLE($(this), oldIndex, newIndex);
	});
	$("div[row='"+oldIndex+"'] textarea").each(function() {
		updateIndexedNameVARIABLE($(this), oldIndex, newIndex);
	});
	$("div[row='"+oldIndex+"']").attr("row", newIndex);
}

function updateIndexedNameVARIABLE(element, oldIndex, newIndex) {
	var name = element.attr("name");
	if (name == null) {
		console.log("name is null in ", element);
		return;
	}
	name = name.replace("[" + oldIndex + "]", "[" + newIndex + "]");
	element.attr("name", name);
}

function updateSizeVARIABLE(delta) {
	var sizeInput = $("input[name='VARIABLE.size']");
	sizeInput.val(parseInt(sizeInput.val()) + delta);
	console.log("List size = " + getSizeVARIABLE());
}

