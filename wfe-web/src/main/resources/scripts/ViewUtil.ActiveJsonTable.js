
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
		
		$(this).tableConstructorUNIQUENAME(
							jsonInputArrayUNIQUENAME, 
							"#containerUNIQUENAME", 
							"SORTFIELDNAMEVALUE", 
							DIMENTIONALVALUE, 
							SELECTABLEVALUE,
							"DECTSELECTNAME");
		
	} catch(e) {
		console.error("ready: %s", e.message);
	}
});

(function($) {
	
	
	var methods = {
	
		"init" : function (input, container, sort, dim, sel, outname) {
			
			console.info("init: input: %s container: %s sort: %s dim: %s sel: %s outname: %s", input, container, sort, dim, sel, outname);
			
			var out;
			
			if (sel) {
				out = $("<input>");
				out.attr("type", "hidden");
				out.attr("name", outname);
				out.attr("value", "");
				$(container).append(out);
			}
			
			$(this).data({
				jsonInputArray : input,	
				sortFieldName : sort,
				containerName : container,
				isSelectable : sel,
				outContainer : out
				});
			
			try {
				for (var i = 0; i < $(this).data().jsonInputArray.length; i++) {
					if (typeof $(this).data().jsonInputArray[i] != "string") {
						stringifyValues($(this).data().jsonInputArray[i]);
						continue;
					}
					$(this).data().jsonInputArray[i] = JSON.parse($(this).data().jsonInputArray[i]);
					stringifyValues($(this).data().jsonInputArray[i]);
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
				var table = $($(this).data().containerName).TidyTable({
					enableCheckbox: $(this).data().isSelectable,
					enableMenu:     false,
					reverseSortDir: false,
					responsive:     false
				},
				{
					columnTitles: titles,
					columnValues: columns,
					sortByPattern: getSortValuePattern,
				});
				if ($(this).data().isSelectable) {
					$(this).tableConstructorUNIQUENAME("setSelectCallbacks", table);
				}
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
				var table = $($(this).data().containerName).TidyTable({
					enableCheckbox: $(this).data().isSelectable,
					enableMenu:     false,
					reverseSortDir: false,
					responsive:     false
				},
				{
					columnTitles: titles,
					columnValues: columns,
					sortByPattern: getSortValuePattern
				});
				if ($(this).data().isSelectable) {
					$(this).tableConstructorUNIQUENAME("setSelectCallbacks", table);
				}
			} catch(e) {
				console.error("makeAsMultiDimentionalTable: %s", e.message);
			}
		},
	
		"setSelectCallbacks" : function(table) {
			try {
				var data = {
						rows : table.find("tr"),
						inputArray : $(this).data().jsonInputArray,
						out : $(this).data().outContainer
				};
				for (var i = 0; i < data.rows.length; i++) {
					var input = $(data.rows[i]).find(":checkbox").first();
					if (!input) {
						return;
					}
					input.bind("click", function() {
						var selectedArray = [];
						for (var i = 0; i < data.inputArray.length; i++) {
							var jsonObjValues = [];
							jsonObjValues[0] = null;
							var jsonObj = data.inputArray[i];
							for (key in jsonObj) {
								jsonObjValues.push(jsonObj[key]);
							}
							for (var j = 0; j < data.rows.length; j++) {
								var match = true;
								var row = $(data.rows[j]).find("td");
								for (var k = 1; k < row.length; k++) {
									try {
										if ($(row[k]).text() != jsonObjValues[k]) {
											match = false;
											break;
										}
									} catch(e) {
										match = false;
										break;
									}
								}
								try {
									if (match && $(data.rows[j]).find(':checkbox').first().prop('checked')) {
										selectedArray.push(jsonObj);
									}
								} catch(e) { 
									console.error("setSelectCallbacks.click: %s", e.message);
								}
							}
						}
						setOutValue(data.out, selectedArray);
					});
				}
			} catch(ex) {
				console.error("setSelectCallbacks: %s", ex.message);
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
	
	function setOutValue(elem, jsonvalobj) {
		try {
			var stringify = "";
			for (var i = 0; i < jsonvalobj.length; i++) {
				if (i != 0) {
					stringify += ", "
				}
				stringify += JSON.stringify(jsonvalobj[i]);
			}
			console.debug("setOutValue: elem: %s stringify: %s", elem, stringify);
			elem.attr("value", stringify);
		} catch(e) {
			console.error("setOutValue: %s", e.message);
		}
	};
	
	function stringifyValues(jsonObj) {
		for (key in jsonObj) {
			if (typeof jsonObj[key] == "string") {
				continue;
			}
			if (jsonObj[key]) {
				jsonObj[key] = jsonObj[key].toString();
			} else {
				jsonObj[key] = "";
			}
		}
	}
	
	function isNumber(val) {
		return typeof val == "number" || (typeof val == "object" && val.constructor === Number);
	};
})(jQuery);

