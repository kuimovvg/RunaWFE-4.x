
var FTL_PLUGIN_NAME = 'FreemarkerTags' ;
var FTL_METHOD_CMD = 'FreemarkerMethod' ;
var FTL_OUTPUT_CMD = 'FreemarkerOutput' ;
var VISUAL_ELEMENT = 'img' ;

// Register the related command.
// FCKPlugins.Items[FTL_PLUGIN_NAME].Path + 'fck_placeholder.html'
FCKCommands.RegisterCommand( FTL_METHOD_CMD, new FCKDialogCommand( FTL_METHOD_CMD, FCKLang.MethodTitle, "/editor/FreemarkerTags.java?method=GetAllMethods", 400, 200 ) ) ;
FCKCommands.RegisterCommand( FTL_OUTPUT_CMD, new FCKDialogCommand( FTL_OUTPUT_CMD, FCKLang.OutputTitle, "/editor/FreemarkerTags.java?method=GetAllVariables", 400, 200 ) ) ;

// Create the "VarTag" toolbar button.
var methodItem = new FCKToolbarButton( FTL_METHOD_CMD, FCKLang.MethodTitle ) ;
methodItem.IconPath = FCKPlugins.Items[FTL_PLUGIN_NAME].Path + 'toolbar.gif' ;
FCKToolbarItems.RegisterItem( FTL_METHOD_CMD, methodItem ) ;

var outputItem = new FCKToolbarButton( FTL_OUTPUT_CMD, FCKLang.OutputTitle ) ;
outputItem.IconPath = FCKPlugins.Items[FTL_PLUGIN_NAME].Path + 'toolbar.gif' ;
FCKToolbarItems.RegisterItem( FTL_OUTPUT_CMD, outputItem ) ;

var FreemarkerTags = new Object() ;

// Add method
FreemarkerTags.AddMethod = function( tagName, params ) {
	var oSpan = FCK.CreateElement( VISUAL_ELEMENT );
	this.SetupSpan( oSpan, tagName, params );
	oSpan.setAttribute("ftltagparams", params);
}

// Add format
FreemarkerTags.AddFormat = function( tagName, format ) {
	var oSpan = FCK.CreateElement( VISUAL_ELEMENT );

	this.SetupSpan( oSpan, tagName, format, "250", "40" );
	oSpan.setAttribute("ftltagformat", format);
}

FreemarkerTags.SetupSpan = function( span, tagName, format, width, height ) {
	if (width == null)
		width = this.GetTagWidth( tagName );
	if (height == null)
		height = this.GetTagHeight( tagName );
	span.setAttribute("src", "http://localhost:48780/editor/FreemarkerTags.java?method=GetTagImage&tagName=" + tagName);
	span.style.width = width;
	span.style.height = height;
	span.setAttribute("ftltagname", tagName);
	// To avoid it to be resized.
	span.onresizestart = function() {
		FCK.EditorWindow.event.returnValue = false ;
		return false ;
	}
}

// On Gecko we must do this trick so the user select all the SPAN when clicking on it.
FreemarkerTags._SetupClickListener = function() {
	FreemarkerTags._ClickListener = function( e ) {
		if ( e.target.ftlTagName ) {
			FCKSelection.SelectNode( e.target ) ;
		}
	}
	FCK.EditorDocument.addEventListener( 'click', FreemarkerTags._ClickListener, true ) ;
}

FCK.ContextMenu.RegisterListener( { AddItems : function( menu, tag, tagName ) {
	if ( tagName && tagName.toLowerCase() == VISUAL_ELEMENT ) {
		menu.AddSeparator();
	}
	if ( tag && tag.getAttribute("ftltagparams") != null) {
		menu.AddItem( FTL_METHOD_CMD, FCKLang.MethodTitle ) ;
	}
	if ( tag && tag.getAttribute("ftltagformat") != null ) {
		menu.AddItem( FTL_OUTPUT_CMD, FCKLang.OutputTitle ) ;
	}
}});

FreemarkerTags.GetTagWidth = function( tagName ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=GetVarTagWidth&tagName=" + tagName, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return 300 ;
	}
}

FreemarkerTags.GetTagHeight = function( tagName ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=GetVarTagHeight&tagName=" + tagName, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return 300 ;
	}
}

FreemarkerTags.GetTagImage = function( tagName ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=GetTagImage&tagName=" + tagName, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return "error" ;
	}
}

FreemarkerTags.GetParameters = function( tagName ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=GetParameters&tagName=" + tagName, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return "Error." ;
	}
}

FreemarkerTags.GetFormats = function( tagName ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=GetFormats&tagName=" + encodeURIComponent(tagName), false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return "Error." ;
	}
}

FreemarkerTags.IsAvailable = function( ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/FreemarkerTags.java?method=IsAvailable", false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return "false";
	}
}

if (FreemarkerTags.IsAvailable() != "true") {
	FCKCommands.GetCommand( FTL_METHOD_CMD ).Execute = function() { return false; };
	FCKCommands.GetCommand( FTL_METHOD_CMD ).GetState = function() { return FCK_TRISTATE_DISABLED; } ;
	FCKCommands.GetCommand( FTL_OUTPUT_CMD ).Execute = function() { return false; };
	FCKCommands.GetCommand( FTL_OUTPUT_CMD ).GetState = function() { return FCK_TRISTATE_DISABLED; } ;
}

