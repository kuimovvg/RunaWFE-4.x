var FTL_PLUGIN_NAME = 'FreemarkerTags';
var FTL_METHOD_CMD = 'FreemarkerMethod';
var FTL_IMG_ELEMENT = 'img';
var FTL_TAG_NAME = 'ftltagname';

var FreemarkerTags = new Object();

FreemarkerTags.GetFromServer = function(methodName, tagType, errorResult) {
    var httpRequest = (!window.XMLHttpRequest)? new ActiveXObject('Microsoft.XMLHTTP') : new XMLHttpRequest();
    httpRequest.open('GET', '/editor/FreemarkerTags.java?method=' + methodName + '&tagName=' + encodeURIComponent(tagType), false);
    httpRequest.send(null);
	if (httpRequest.status == 200 || httpRequest.status == 304) {
		return httpRequest.responseText;
	} else {
		return errorResult ;
	}
}

FreemarkerTags.GetTagWidth = function(tagType) {
	return FreemarkerTags.GetFromServer('GetVarTagWidth', tagType, '250');
}
FreemarkerTags.GetTagHeight = function(tagType) {
	return FreemarkerTags.GetFromServer('GetVarTagHeight', tagType, '40');
}
FreemarkerTags.GetTagImage = function(tagType) {
	return FreemarkerTags.GetFromServer('GetTagImage', tagType, 'error');
}
FreemarkerTags.GetParameters = function(tagType) {
	return FreemarkerTags.GetFromServer('GetParameters', tagType, 'Error.');
}
FreemarkerTags.GetFormats = function(tagType) {
	return FreemarkerTags.GetFromServer('GetFormats', tagType, 'Error.');
}

FreemarkerTags.createFakeParserElement = function(realElement) {
	var writer = new CKEDITOR.htmlParser.basicWriter();
	realElement.writeHtml(writer);
	var html = writer.getHtml();
	var attributes = {
		'class': 'cke_' + 'ftl_element',
		'data-cke-real-node-type': CKEDITOR.NODE_ELEMENT,
		'cke-real-element-type': 'ftl_element', 
		'data-cke-realelement': encodeURIComponent(html),
		'cke_resizable': false, 
		height: FreemarkerTags.GetTagHeight(realElement.attributes[FTL_TAG_NAME]),
		width: FreemarkerTags.GetTagWidth(realElement.attributes[FTL_TAG_NAME]),
		src : 'http://localhost:48780/editor/FreemarkerTags.java?method=GetTagImage&tagName=' + realElement.attributes[FTL_TAG_NAME],
		style: 'background-color: #ffff00; color: #000000;'
	};
	return new CKEDITOR.htmlParser.element(FTL_IMG_ELEMENT, attributes);
};

CKEDITOR.plugins.add(FTL_PLUGIN_NAME, {
		lang: 'en,ru',
		requires: 'dialog,fakeobjects',
		beforeInit : function(editor) {	
			// Register own menu group.
			editor.config.menu_groups = editor.config.menu_groups + ',runawfe';
		},
		init: function(editor) {
			editor.addCommand(FTL_METHOD_CMD, new CKEDITOR.dialogCommand(FTL_METHOD_CMD));
			editor.ui.addButton(FTL_METHOD_CMD, {label:editor.lang.FreemarkerTags.MethodTitle, icon: this.path + 'toolbar.gif', command:FTL_METHOD_CMD});
			//CKEDITOR.dialog.add(FTL_METHOD_CMD, 'http://localhost:48780/plugins/FreemarkerTags/dialog.js');
			CKEDITOR.dialog.add(FTL_METHOD_CMD, 'http://localhost:48780/editor/FreemarkerTags.java?method=GetMethodDialog');
			if (editor.addMenuItems) {	
				// If the 'menu' plugin is loaded, register the menu items.
				editor.addMenuItems({
					'ftl_element': {
						label : editor.lang.FreemarkerTags.MethodTitle,
						command : FTL_METHOD_CMD,
						group : 'flash'
					}
				});
			}
			editor.on( 'doubleclick', function( evt ) {
				var element = evt.data.element;
				if (element && element.getAttribute('cke-real-element-type') == 'ftl_element' ) {
					evt.data.dialog = FTL_METHOD_CMD;
				}
			} );
			if (editor.contextMenu) {
				// If the 'contextmenu' plugin is loaded, register the listeners.
				editor.contextMenu.addListener(function(element, selection) {
					if (element && element.getAttribute('cke-real-element-type') == 'ftl_element') {
						return { 'ftl_element': CKEDITOR.TRISTATE_OFF };
					}
				});
			}
		},
		afterInit: function(editor) {
			var dataProcessor = editor.dataProcessor, dataFilter = dataProcessor && dataProcessor.dataFilter;
			if ( dataFilter ) {
				dataFilter.addRules( {
					elements: {
						'ftl_element': function(element) {
							return FreemarkerTags.createFakeParserElement(element);
						}
					}
				}, {priority: 4, applyToAll: true} );
			}
		}
	}
);
