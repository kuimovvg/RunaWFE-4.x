
var jsonInputArrayUNIQUENAME = JSONDATATEMPLATE;

if (!window.console) {
	window.console = {};
}

if (!window.console.log) {
	window.console.log = function(msg) {};
}
if (!window.console.info) {
	window.console.info = function(msg) {};
}
if (!window.console.debug) {
	window.console.debug = function(msg) {};
}
if (!window.console.error) {
	window.console.error = function(msg) {};
}

$(document).ready(function() {
	
	try {
		
		$(this).tableConstructorUNIQUENAME(jsonInputArrayUNIQUENAME, "SORTFIELDNAMEVALUE", DIMENTIONALVALUE);
		
	} catch(e) {
		console.error("ready: %s", e.message);
	}
});

(function($) {
	
	
	var methods = {
	
		"init" : function (input, sort, dim) {
			
			console.info("init: input: %s sort: %s dim: %s", input, sort, dim);
			
			$(this).data({
				jsonInputArray : input,	
				sortFieldName : sort,
				containerName : "#containerUNIQUENAME"
				});
			
			try {
				for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
					if (typeof $(this).data().jsonInputArray[i] != "string") {
						continue;
					}
					$(this).data().jsonInputArray[i] = JSON.parse($(this).data().jsonInputArray[i]);
				}
			} catch(e) {
				console.error("init: %s", e.message);
			}
			
			if (dim) {
				$(this).tableConstructorUNIQUENAME("makeAsMultiDimentionalTable");
			} else {
				$(this).tableConstructorUNIQUENAME("makeAsTwoDimentionalTable");
			}
		},
		
		"getPreferedTitles" : function () {
			
			var titles = [];
			for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
				for (var field in $(this).data().jsonInputArray[i]) {
					if ($.inArray(field, titles) == -1) {
						titles.push(field);
					}
				}
			}
			return titles;
		},
		
		"makeAsTwoDimentionalTable" : function () {
			
			var titles = $(this).tableConstructorUNIQUENAME("getPreferedTitles");
			var columns = [];
			try {
				for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
					var row = [];
					for (var j = 0; j < titles.length; j++) {
						if ($(this).data().jsonInputArray[i][titles[j]]) {
							row.push($(this).data().jsonInputArray[i][titles[j]]);
						} else {
							row.push("");
						}
					}
					columns.push(row);
				}
				sortRows($.inArray($(this).data().sortFieldName, titles), columns);
				$($(this).data().containerName).TidyTable({
					enableCheckbox: SELECTABLEVALUE,
					enableMenu:     false,
					reverseSortDir: false,
					responsive:     false
				},
				{
					columnTitles: titles,
					columnValues: columns,
					sortByPattern: getSortValuePattern
				});
			} catch(e) {
				console.error("makeAsTwoDimentionalTable: %s", e.message);
			}
		},
		
		"makeAsMultiDimentionalTable" : function () {
			
			var titles = [];
			var columns = [];
			var fields = $(this).tableConstructorUNIQUENAME("getPreferedTitles");
			try {
				if ($(this).data().sortFieldName != "null" && 
						$.inArray($(this).data().sortFieldName, fields) != -1) {
					titles.push($(this).data().sortFieldName);
					titles.push("");
				}
				for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
					var subrows = [];
					for (var j = 0; j < fields.length; j++) {
						if ($(this).data().jsonInputArray[i][fields[j]]) {
							subrows.push([fields[j], $(this).data().jsonInputArray[i][fields[j]]]);
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
					if ($(this).data().sortFieldName != "null") {
						if ($(this).data().jsonInputArray[i][$(this).data().sortFieldName]) {
							columns.push([$(this).data().jsonInputArray[i][$(this).data().sortFieldName], subtable]);
						} else {
							columns.push(["", subtable]);
						}
					} else {
						columns.push([subtable]);
					}
				}
				if ($(this).data().sortFieldName != "null" && 
						$.inArray($(this).data().sortFieldName, fields) != -1) {
					sortRows(0, columns);
				}
				$($(this).data().containerName).TidyTable({
					enableCheckbox: SELECTABLEVALUE,
					enableMenu:     false,
					reverseSortDir: false,
					responsive:     false
				},
				{
					columnTitles: titles,
					columnValues: columns,
					sortByPattern: getSortValuePattern
				});
			} catch(e) {
				console.error("makeAsMultiDimentionalTable: %s", e.message);
			}
		}
	
	};
	
	$.fn.tableConstructorUNIQUENAME = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else
		if (typeof method === "object" || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			console.error("call: method: %s does not exist", method);
		}
	};
	
	function sortRows (col_num, rows) {
		if (rows.length <= 1 || col_num < 0 || col_num >= rows[0].length) {
			return;
		}
		rows.sort(function(a, b) {
			var v1 = getSortValuePattern(col_num, a[col_num]);
			var v2 = getSortValuePattern(col_num, b[col_num]);
			return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
		});
	}
	
	function getSortValuePattern(col_num, val) {
		/*int pattern*/
		if (isNumber(val)) {
			return val;
		}
		/*skip objects*/
		if (typeof val != "string") {
			val = val.toString();
		}
		/*direct convert to number*/
		try {
			var num = parseInt(val);
			if (!(/\D/g).test(val) && isNumber(num)) {
				return num;
			}
		} catch(e) {
			
		}
		/*other convert to number patterns*/
		var res;
		try {
			/*date pattern*/
			var datere = /(\d{2})\.(\d{2})\.(\d{4})\s+(\d{2}):(\d{2})/g;
			var matchdate = datere.exec(val)
			if (matchdate) {
				res = matchdate[3] + matchdate[2] + matchdate[1] + matchdate[4] + matchdate[5];
				res = parseInt(res);
			} else {
				res = val;
			}
		} catch(e) {
			return val;
		}
		return res;
	};
	
	function isNumber(val) {
		return typeof val == "number" || (typeof val == "object" && val.constructor === Number);
	};
	
})(jQuery);