
var jsonInputArrayUNIQUENAME = JSONDATATEMPLATE;
var isMultiDimentionalUNIQUENAME = DIMENTIONALVALUE;
var sortFieldNameUNIQUENAME = "SORTFIELDNAMEVALUE";

$(document).ready(function() {
	try {
		console.debug("ready: isMultiDimentional: %s", isMultiDimentionalUNIQUENAME);
		convertStringsToJsonObjects();
		if (isMultiDimentionalUNIQUENAME) {
			makeAsMultiDimentionalTable();
		} else {
			makeAsTwoDimentionalTable();
		}
	} catch(e) {
		console.error("ready: %s", e.message);
	}
});

function getPreferedTitles() {
	var titles = [];
	for (var i = 0; i < jsonInputArrayUNIQUENAME.length; i++) {
		for (var field in jsonInputArrayUNIQUENAME[i]) {
			if ($.inArray(field, titles) == -1) {
				titles.push(field);
			}
		}
	}
	return titles;
}

function makeAsTwoDimentionalTable() {
	var titles = getPreferedTitles();
	var columns = [];
	try {
		for (var i = 0; i < jsonInputArrayUNIQUENAME.length; i++) {
			var row = [];
			for (var j = 0; j < titles.length; j++) {
				if (jsonInputArrayUNIQUENAME[i][titles[j]]) {
					row.push(jsonInputArrayUNIQUENAME[i][titles[j]]);
				} else {
					row.push("");
				}
			}
			columns.push(row);
		}
		$("#containerUNIQUENAME").TidyTable({
			enableCheckbox: SELECTABLEVALUE,
			enableMenu:     false,
			reverseSortDir: false,
			responsive:     false
		},
		{
			columnTitles: titles,
			columnValues: columns
		});
	} catch(e) {
		console.error("makeAsTwoDimentionalTable: %s", e.message);
	}
}

function makeAsMultiDimentionalTable() {
	var titles = [];
	var columns = [];
	var fields = getPreferedTitles();
	try {
		console.debug("makeAsMultiDimentionalTable: sortFieldName: %s length: %s", sortFieldNameUNIQUENAME, jsonInputArrayUNIQUENAME.length);
		if (sortFieldNameUNIQUENAME != "null" && $.inArray(sortFieldNameUNIQUENAME, fields) != -1) {
			titles.push(sortFieldNameUNIQUENAME);
			titles.push("");
		}
		console.debug("makeAsMultiDimentionalTable: titles: %s", titles);
		for (var i = 0; i < jsonInputArrayUNIQUENAME.length; i++) {
			var subrows = [];
			for (var j = 0; j < fields.length; j++) {
				if (jsonInputArrayUNIQUENAME[i][fields[j]]) {
					subrows.push([fields[j], jsonInputArrayUNIQUENAME[i][fields[j]]]);
				} else {
					subrows.push([fields[j], ""]);
				}
			}
			var subtable = $("<div>").TidyTable({
				enableCheckbox: false,
				enableMenu:     false,
				reverseSortDir: false,
				responsive:     false
			},
			{
				columnTitles: [],
				columnValues: subrows
			});
			if (sortFieldNameUNIQUENAME != "null" && jsonInputArrayUNIQUENAME[i][sortFieldNameUNIQUENAME]) {
				columns.push([jsonInputArrayUNIQUENAME[i][sortFieldNameUNIQUENAME], subtable]);
			} else {
				columns.push([subtable]);
			}
			console.debug("makeAsMultiDimentionalTable: subtable: %s", subtable);
		}
		$("#containerUNIQUENAME").TidyTable({
			enableCheckbox: SELECTABLEVALUE,
			enableMenu:     false,
			reverseSortDir: false,
			responsive:     false
		},
		{
			columnTitles: titles,
			columnValues: columns
		});
	} catch(e) {
		console.error("makeAsMultiDimentionalTable: %s", e.message);
	}
}

function convertStringsToJsonObjects() {
	try {
		for (var i = 0; i < jsonInputArrayUNIQUENAME.length; i++) {
			console.debug("convertStringsToJsonObjects: entry: %s", jsonInputArrayUNIQUENAME[i]);
			if (typeof jsonInputArrayUNIQUENAME[i] != 'string') {
				continue;
			}
			jsonInputArrayUNIQUENAME[i] = JSON.parse(jsonInputArrayUNIQUENAME[i]);
		}
	} catch(e) {
		console.error("convertStringsToJsonObjects: %s", e.message);
	}
}