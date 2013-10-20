var FTL_PLUGIN_NAME = 'FreemarkerTags' ;
var FTL_METHOD_CMD = 'FreemarkerMethod' ;
var FTL_OUTPUT_CMD = 'FreemarkerOutput' ;
var FTL_VISUAL_ELEMENT = 'img' ;
var FTL_TYPE_ATTR = 'ftltagname';

var FreemarkerTags = new Object() ;

// Get HTTP request and return result (responseText) 
FreemarkerTags.GetFromServer = function( methodName, tagType, errorResult ){
    var httpRequest = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
    httpRequest.open( "GET", "/editor/FreemarkerTags.java?method=" + methodName + "&tagName=" + encodeURIComponent(tagType), false ) ;
    httpRequest.send(null);
	if ( httpRequest.status == 200 || httpRequest.status == 304 ) {
		return httpRequest.responseText;
	} else {
		return errorResult ;
	}
}

FreemarkerTags.GetTagWidth = function( tagType ) {return FreemarkerTags.GetFromServer( "GetVarTagWidth", tagType, "250" );}
FreemarkerTags.GetTagHeight = function( tagType ) {return FreemarkerTags.GetFromServer( "GetVarTagHeight", tagType, "40" );}
FreemarkerTags.GetTagImage = function( tagType ) {return FreemarkerTags.GetFromServer( "GetTagImage", tagType, "error" );}
FreemarkerTags.GetParameters = function( tagType ) {return FreemarkerTags.GetFromServer( "GetParameters", tagType, "Error.");}
FreemarkerTags.GetFormats = function( tagType ) {return FreemarkerTags.GetFromServer( "GetFormats", tagType, "Error.");}

FreemarkerTags.OpenComponentHelp = function( tagName ) {
  var oXmlHttp = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=OpenComponentHelp&tagName=" + tagName, false ) ;
  oXmlHttp.send( null ) ;
  if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
    return oXmlHttp.responseText ;
  } else {
    return "Error." ;
  }
};

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

FreemarkerTags.SynchronizeIds = function () {
  var oXmlHttp = (!window.XMLHttpRequest) ? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open("GET", "/editor/FreemarkerTags.java?method=SynchronizeIds", false);
  oXmlHttp.send(null);
  if (oXmlHttp.status == 200 || oXmlHttp.status == 304) {
    return oXmlHttp.responseText;
  }
  else {
    return "Error.";
  }
};

FreemarkerTags.createFakeParserElement = function( realElement, className, realElementType )
{
	var writer = new CKEDITOR.htmlParser.basicWriter();
	realElement.writeHtml( writer );
	var html = writer.getHtml();
	var attributes =
	{
      'id' : FreemarkerTags.GetComponentId(realElement.attributes[FTL_TYPE_ATTR]),
      'class' : className,
		_cke_realelement : encodeURIComponent( html ),
		height: FreemarkerTags.GetTagHeight(realElement.attributes[FTL_TYPE_ATTR]),
		width: FreemarkerTags.GetTagWidth(realElement.attributes[FTL_TYPE_ATTR]),
		src : "http://localhost:48780/editor/FreemarkerTags.java?method=GetTagImage&tagName=" + realElement.attributes[FTL_TYPE_ATTR],
		style: "background-color: #ffff00; color: #000000;"
	};
	if ( realElementType )
		attributes._cke_real_element_type = realElementType;
	attributes._cke_resizable = false;
	return new CKEDITOR.htmlParser.element( 'img' , attributes);
};

CKEDITOR.plugins.add(
	FTL_PLUGIN_NAME,
	{
		lang : [ 'en', 'ru' ],
		beforeInit : function( editor )
		{	// Register own menu group.
			editor.config.menu_groups = editor.config.menu_groups + ',runawfe';
		},
		init:function(editor){
			var isAvailable = "false";
			{
			    var httpRequest = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
			    httpRequest.open( "GET", "/editor/FreemarkerTags.java?method=IsAvailable", false ) ;
			    httpRequest.send(null);
				if ( httpRequest.status == 200 || httpRequest.status == 304 ) {
					isAvailable = httpRequest.responseText;
					//alert(isAvailable);
				}
			}

			editor.addCommand(FTL_METHOD_CMD, new CKEDITOR.dialogCommand(FTL_METHOD_CMD));
			if(isAvailable == "true")
				editor.ui.addButton(FTL_METHOD_CMD, {label:editor.lang.FreemarkerTags.MethodTitle, icon: this.path + "toolbar.gif", command:FTL_METHOD_CMD});
			CKEDITOR.dialog.add(FTL_METHOD_CMD, 'http://localhost:48780/editor/FreemarkerTags.java?method=GetMethodDialog');

			editor.addCommand(FTL_OUTPUT_CMD, new CKEDITOR.dialogCommand(FTL_OUTPUT_CMD));
			if(isAvailable == "true")
				editor.ui.addButton(FTL_OUTPUT_CMD, {label:editor.lang.FreemarkerTags.MethodTitle, icon: this.path + "toolbar.gif", command:FTL_OUTPUT_CMD});
			CKEDITOR.dialog.add(FTL_OUTPUT_CMD, 'http://localhost:48780/editor/FreemarkerTags.java?method=GetOutputDialog');

			if ( editor.addMenuItems )
			{	// If the "menu" plugin is loaded, register the menu items.
/*				editor.addMenuItems(
				{
					ftl_element :
					{
						label : editor.lang.FreemarkerTags.MethodTitle,
						command : FTL_METHOD_CMD,
						group : 'runawfe'
					}
				});*/
				editor.addMenuItems(
				{
					ftl_element_output :
					{
						label : editor.lang.FreemarkerTags.OutputTitle,
						command : FTL_OUTPUT_CMD,
						group : 'runawfe'
					}
				});
			}
			if ( editor.contextMenu )
			{ 	// If the "contextmenu" plugin is loaded, register the listeners.
/*				editor.contextMenu.addListener( function( element, selection )
					{
						if ( element && element.getAttribute( '_cke_real_element_type' ) == 'ftl_element' ){
							return { ftl_element : CKEDITOR.TRISTATE_ON };
						}
					});*/

				editor.contextMenu.addListener( function( element, selection )
						{
							if ( element && element.getAttribute( '_cke_real_element_type' ) == 'ftl_element_output' ){
								return { ftl_element_output : CKEDITOR.TRISTATE_ON };
							}
						});
			}
		},
		afterInit : function( editor )
		{
          editor.on('selectionChange', function () {
            var selection = editor.getSelection();
            var selectedElement = selection.getSelectedElement();
            if (!selectedElement || selectedElement.$.className.indexOf("ftl_component") == -1)
              FreemarkerTags.ComponentDeselected();
            else
              FreemarkerTags.ComponentSelected(selectedElement.$.id);
          });

          editor.on('paste', function () {
            FreemarkerTags.SynchronizeIds();
          }, editor.element.$);


			var dataProcessor = editor.dataProcessor,
				dataFilter = dataProcessor && dataProcessor.dataFilter;
			if ( dataFilter )
			{
				dataFilter.addRules(
				{
					elements :
					{
						ftl_element : function( element )
						{
							return FreemarkerTags.createFakeParserElement( element, 'cke_' + 'ftl_element ftl_component', 'ftl_element');
						}
					}
				});
				dataFilter.addRules(
				{
					elements :
					{
						ftl_element_output : function( element )
						{
							return FreemarkerTags.createFakeParserElement( element, 'cke_' + 'ftl_element_output ftl_component', 'ftl_element_output');
						}
					}
				});
			}
		},

		requires : [ 'fakeobjects' ]
	}
);
