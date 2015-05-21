
var jsonInputArrayUNIQUENAME = JSONDATATEMPLATE;
var isMultiDimentionalUNIQUENAME = DIMENTIONALVALUE;
var sortFieldNameUNIQUENAME = SORTFIELDNAMEVALUE;

$(document).ready(function() {
	try {
		convertStringsToJsonObjects();
		if (isMultiDimentionalUNIQUENAME) {
			makeAsMultiDimentionalTable();
		} else {
			makeAsTwoDimentionalTable();
		}
	} catch(e) {
		alert("ready: " + e.message);
	}
});

function makeAsTwoDimentionalTable() {
	var titles = [];
	var columns = [];
	try {
		if (jsonInputArrayUNIQUENAME.length <= 0) {
			return;
		}
		for (var field in jsonInputArrayUNIQUENAME[0]) {
			titles.push(field);
		}
		for (var i = 0; i < jsonInputArrayUNIQUENAME.length; i++) {
			var row = [];
			for (var j = 0; j < titles.length; j++) {
				row.push(jsonInputArrayUNIQUENAME[i][titles[j]]);
			}
			columns.push(row);
		}
		$('#containerUNIQUENAME').TidyTable({
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
		alert("makeAsTwoDimentionalTable: " + e.message);
	}
}

function makeAsMultiDimentionalTable() {
	
}

function convertStringsToJsonObjects() {
	try {
		for (var i = 0; i < jsonInputArrayUNIQUENAME.length; i++) {
			if (typeof jsonInputArrayUNIQUENAME[i] != 'string') {
				continue;
			}
			jsonInputArrayUNIQUENAME[i] = JSON.parse(jsonInputArrayUNIQUENAME[i]);
		}
	} catch(e) {
		alert("convertStringsToJsonObjects: " + e.message);
	}
}