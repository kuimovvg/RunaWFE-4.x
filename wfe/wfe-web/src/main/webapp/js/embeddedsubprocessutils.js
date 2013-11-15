$(document).ready(function() {
	// http://jqueryui.com/tooltip/	
	$(document).tooltip({ 
		track: true
	});
});

function showEmbeddedSubprocessDefinition(definitionId, subprocessId) {
	var src = "/wfe/processDefinitionGraphImage.do?id=" + definitionId + "&name=" + subprocessId;
	showImageDialog(src);
}

function showEmbeddedSubprocess(processId, subprocessId) {
	var src = "/wfe/processGraphImage.do?id=" + processId + "&name=" + subprocessId;
	showImageDialog(src);
}

function showImageDialog(src) {
	var html = "<div><img src="+unify(src)+"></div>";
    $(html).dialog({ 
    	modal: true,
    	position: [100, 100],  
    	width: 'auto'
    });
}
