
var editLinkedListsVariableNames = [VARIABLE_NAMES];
var editLinkedListsRowTemplate = "ROW_TEMPLATE";

$(document).ready(function() {
    $('#editLinkedListsButtonAdd').click(function() {
        var rowIndex = getEditLinkedLinksSize();
		console.log("Adding row " + rowIndex);
        var e = "<tr row='" + rowIndex + "'>";
        e += editLinkedListsRowTemplate.replace(/\[\]/g, "[" + rowIndex + "]");
        e += "<td><input type='button' value=' - ' onclick='removeRow(this);' /></td>";
        e += "</tr>";
        $('#editLinkedLists').append(e);
        updateSize(1);
        JS_HANDLERS
    });
});

function getEditLinkedLinksSize() {
	return parseInt($("#editLinkedLists").attr("rowsCount"));
}

function removeRow(button) {
	var tr = $(button).closest("tr");
	var rowIndex = parseInt(tr.attr("row"));
	var size = getEditLinkedLinksSize();
	console.log("Removing row ", rowIndex);
	tr.remove();
	for (var i = rowIndex; i < size - 1; i++) {
		updateRowIndexes(i + 1, i);
	}
	updateSize(-1);
}

function updateRowIndexes(oldIndex, newIndex) {
	$("tr[row='"+oldIndex+"'] input").each(function() {
		updateIndexedName($(this), oldIndex, newIndex);
	});
	$("tr[row='"+oldIndex+"'] select").each(function() {
		updateIndexedName($(this), oldIndex, newIndex);
	});
	$("tr[row='"+oldIndex+"'] textarea").each(function() {
		updateIndexedName($(this), oldIndex, newIndex);
	});
	$("tr[row='"+oldIndex+"']").attr("row", newIndex);
}

function updateIndexedName(element, oldIndex, newIndex) {
	var name = element.attr("name");
	if (name == null) {
		console.log("name is null in ", element);
		return;
	}
	name = name.replace("[" + oldIndex + "]", "[" + newIndex + "]");
	element.attr("name", name);
}

function updateSize(delta) {
    for (i in editLinkedListsVariableNames) {
		var sizeInput = $("input[name='" + editLinkedListsVariableNames[i] + ".size']");
		sizeInput.val(parseInt(sizeInput.val()) + delta);
	}
	$("#editLinkedLists").attr("rowsCount", getEditLinkedLinksSize() + delta);
	console.log("Lists size = " + getEditLinkedLinksSize());
}
