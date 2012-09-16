var VAR_TAG_PLUGIN_NAME = 'RunaVarTags' ;
var VAR_TAG = 'customtag' ;
var VT_NAME_ATTR = 'var' ;
var VT_TYPE_ATTR = 'delegation' ;

var RunaVarTags = new Object();
// Get HTTP request and return result (responseText) 
RunaVarTags.GetFromServer = function( methodName, tagType, errorResult ){
    var httpRequest = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
    httpRequest.open( "GET", "/editor/RunaVarTags.java?method=" + methodName + "&type=" + tagType, false ) ;
    httpRequest.send(null);
	if ( httpRequest.status == 200 || httpRequest.status == 304 ) {
		return httpRequest.responseText;
	} else {
		return errorResult ;
	}
}

RunaVarTags.IsTagHaveImage = function( tagType ) {return RunaVarTags.GetFromServer( "IsTagHaveImage", tagType, "" ) == "true";}
RunaVarTags.GetTagVisibleName = function( tagType ) {return RunaVarTags.GetFromServer( "GetTagVisibleName", tagType, "unknown" );}
RunaVarTags.GetTagWidth = function( tagType ) {return RunaVarTags.GetFromServer( "GetVarTagWidth", tagType, "150" );}
RunaVarTags.GetTagHeight = function( tagType ) {return RunaVarTags.GetFromServer( "GetVarTagHeight", tagType, "40" );}

RunaVarTags.createFakeParserElement = function( realElement, className, realElementType )
{
	var writer = new CKEDITOR.htmlParser.basicWriter();
	realElement.writeHtml( writer );
	var html = writer.getHtml();
	var attributes =
	{
		'class' : className,
		_cke_realelement : encodeURIComponent( html ),
		height: RunaVarTags.GetTagHeight(realElement.attributes[VT_TYPE_ATTR]),
		width: RunaVarTags.GetTagWidth(realElement.attributes[VT_TYPE_ATTR]),
		src : "http://localhost:48780/editor/RunaVarTags.java?method=GetTagImage&type=" + realElement.attributes[VT_TYPE_ATTR],
		alt: '[' + RunaVarTags.GetTagVisibleName(realElement.attributes[VT_TYPE_ATTR]) + ']',
		style: "background-color: #ffff00; color: #000000;"
	};

	if ( realElementType )
		attributes._cke_real_element_type = realElementType;
	attributes._cke_resizable = false;

	return new CKEDITOR.htmlParser.element( 'img' , attributes);
};

CKEDITOR.plugins.add(
	VAR_TAG_PLUGIN_NAME,
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
			    httpRequest.open( "GET", "/editor/RunaVarTags.java?method=IsAvailable", false ) ;
			    httpRequest.send(null);
				if ( httpRequest.status == 200 || httpRequest.status == 304 ) {
					isAvailable = httpRequest.responseText;
				}
			}

			editor.addCommand(VAR_TAG, new CKEDITOR.dialogCommand(VAR_TAG));
			if(isAvailable == "true")
				editor.ui.addButton(VAR_TAG_PLUGIN_NAME, {label:editor.lang.RunaVarTags.VarTagBtn, icon: this.path + "toolbar.gif", command:VAR_TAG});
			CKEDITOR.dialog.add(VAR_TAG, "http://localhost:48780/editor/RunaVarTags.java?method=GetVarTagDialog");

			if ( editor.addMenuItems )
			{	// If the "menu" plugin is loaded, register the menu items.
				editor.addMenuItems(
					{
						customtag :
						{
							label : editor.lang.RunaVarTags.VarTagDlgTitle,
							command : VAR_TAG,
							group : 'runawfe'
						}
					});
			}

			if ( editor.contextMenu )
			{ 	// If the "contextmenu" plugin is loaded, register the listeners.
				editor.contextMenu.addListener( function( element, selection )
					{
						if ( element && element.getAttribute( '_cke_real_element_type' ) == 'customtag' ){
							return { customtag : CKEDITOR.TRISTATE_ON };
						}
					});
			}

		},
		afterInit : function( editor )
		{
			var dataProcessor = editor.dataProcessor,
				dataFilter = dataProcessor && dataProcessor.dataFilter;
	
			if ( dataFilter )
			{
				dataFilter.addRules(
				{
					elements :
					{
						customtag : function( element )
						{
							return RunaVarTags.createFakeParserElement( element, 'cke_' + VAR_TAG, VAR_TAG);
						}
					}
				});
			}
		},

		requires : [ 'fakeobjects' ]
	}
);
