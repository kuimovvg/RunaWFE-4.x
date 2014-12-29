var FTL_PLUGIN_NAME = 'FreemarkerTags';
var FTL_METHOD_CMD = 'FreemarkerMethod';
var FTL_TAG_NAME = 'ftltagname';

var FreemarkerTags = new Object();
FreemarkerTags.SelectedId = {id : -1};

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

FreemarkerTags.GetParameters = function(tagType) {
	return FreemarkerTags.GetFromServer('GetParameters', tagType, 'Error.');
}
FreemarkerTags.GetFormats = function(tagType) {
	return FreemarkerTags.GetFromServer('GetFormats', tagType, 'Error.');
}

FreemarkerTags.ComponentSelected = function( componentId ) {
  var oXmlHttp = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=ComponentSelected&componentId=" + componentId, false ) ;
  oXmlHttp.send( null ) ;
  if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
    return oXmlHttp.responseText ;
  } else {
    return "Error." ;
  }
};

FreemarkerTags.ComponentDeselected = function() {
  var oXmlHttp = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=ComponentDeselected", false ) ;
  oXmlHttp.send( null ) ;
  if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
    return oXmlHttp.responseText ;
  } else {
    return "Error." ;
  }
};

FreemarkerTags.TagDialog = function() {
  var oXmlHttp = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=TagDialog&componentId=" + FreemarkerTags.SelectedId.id, false ) ;
  oXmlHttp.send( null ) ;
  if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
    return oXmlHttp.responseText ;
  } else {
    return "Error." ;
  }
};

FreemarkerTags.GetComponentId = function (tagName) {
  var oXmlHttp = (!window.XMLHttpRequest) ? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open("GET", "/editor/FreemarkerTags.java?method=CreateComponent&tagName=" + tagName, false);
  oXmlHttp.send(null);
  if (oXmlHttp.status == 200 || oXmlHttp.status == 304) {
    return JSON.parse(oXmlHttp.responseText).componentId;
  }
  else {
    return "Error.";
  }
};

FreemarkerTags.createFakeParserElement = function(realElement) {
	var writer = new CKEDITOR.htmlParser.basicWriter();
	realElement.writeHtml(writer);
	var html = writer.getHtml();
	var id = realElement.attributes['id'];
	var attributes = {
	    id : id != 'undefined' ? id : FreemarkerTags.GetComponentId(realElement.attributes[FTL_TAG_NAME]),
		'class': 'cke_' + 'ftl_element',
		'data-cke-real-node-type': CKEDITOR.NODE_ELEMENT,
		'cke-real-element-type': 'ftl_element', 
		'data-cke-realelement': encodeURIComponent(html),
		'cke_resizable': false, 
		src: 'http://localhost:48780/editor/FreemarkerTags.java?method=GetTagImage&tagName=' + realElement.attributes[FTL_TAG_NAME] + "&tagParams=" + encodeURIComponent(realElement.attributes['ftltagparams']),
		style: 'margin: 3px;'
	};
	return new CKEDITOR.htmlParser.element('img', attributes);
};

CKEDITOR.plugins.add(FTL_PLUGIN_NAME, {
		lang: 'en,ru',
		requires: 'dialog,fakeobjects',
		beforeInit : function(editor) {	
			// Register own menu group.
			editor.config.menu_groups = editor.config.menu_groups + ',runawfe';
		},
		init: function(editor) {
			editor.addCommand(FTL_METHOD_CMD, {exec: function(){FreemarkerTags.TagDialog();}});
			editor.ui.addButton(FTL_METHOD_CMD, {label:editor.lang.FreemarkerTags.MethodTitle, icon: this.path + 'toolbar.gif', command:FTL_METHOD_CMD});
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
			editor.on('selectionChange', function () {
				var selection = editor.getSelection();
            var selectedElement = selection.getSelectedElement();
            if (!selectedElement || selectedElement.$.className.indexOf('cke_ftl_element') == -1){
              FreemarkerTags.ComponentDeselected();
              FreemarkerTags.SelectedId.id = -1;
            }else{
              FreemarkerTags.ComponentSelected(selectedElement.$.id);
              FreemarkerTags.SelectedId.id = selectedElement.$.id;
            }
		});
			
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
