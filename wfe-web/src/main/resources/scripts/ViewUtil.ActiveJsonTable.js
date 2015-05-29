
var jsonInputArrayUNIQUENAME = JSONDATATEMPLATE;

$(document).ready(function() {
	
	if (!window.console) {
		
		window.console = {};
		
		var safeconsole = {
			log : function(msg) {},	
			info : function(msg) {},
			debug : function(msg) {},
			error : function(msg) {}
		};
			
		$.extend(window.console, safeconsole);
	}
	
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
					console.debug("constructor: entry: %s", $(this).data().jsonInputArray[i]);
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
				$($(this).data().containerName).TidyTable({
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
		},
		
		"makeAsMultiDimentionalTable" : function () {
			
			var titles = [];
			var columns = [];
			var fields = $(this).tableConstructorUNIQUENAME("getPreferedTitles");
			try {
				console.debug("makeAsMultiDimentionalTable: sortFieldName: %s length: %s", 
									$(this).data().sortFieldName, 
									$(this).data().jsonInputArray.length);
				if ($(this).data().sortFieldName != "null" && $.inArray($(this).data().sortFieldName, fields) != -1) {
					titles.push($(this).data().sortFieldName);
					titles.push("");
				}
				console.debug("makeAsMultiDimentionalTable: titles: %s", titles);
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
					if ($(this).data().sortFieldName != "null" && $(this).data().jsonInputArray[i][$(this).data().sortFieldName]) {
						columns.push([$(this).data().jsonInputArray[i][$(this).data().sortFieldName], subtable]);
					} else {
						columns.push([subtable]);
					}
					console.debug("makeAsMultiDimentionalTable: subtable: %s", subtable);
				}
				$($(this).data().containerName).TidyTable({
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
	
	};
	
	$.fn.tableConstructorUNIQUENAME = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else
		if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			console.error('call: method: %s does not exist', method);
		}
	};
	
})(jQuery);