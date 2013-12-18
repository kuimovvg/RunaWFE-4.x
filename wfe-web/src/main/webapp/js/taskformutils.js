
$(function() {
	$(document).bind('drop dragover', function (e) {
		e.preventDefault();
	});
	$('.dropzone').each(function () {
		initFileInput($(this));
	});
	$(".dropzone").bind("dragleave dragend drop", function (e) {
		$(this).removeClass("dropzonehover");
	});
	$(".dropzone").bind('dragover', function (e) {
		$(this).addClass("dropzonehover");
	});

	$(".inputFileAttach").click(function(e) {
		$(this).parent().find(".inputFile").click();
//		$(".inputFile").fileupload("add", {
//			files: e.target.files || [{name: this.value}],
//			fileInput: $(this)
//		});
	});
	$(document).delegate(".inputFileDelete", "click", function() {
		deleteFile($(this).attr("inputId"));
	});
});

function initFileInput(dropzone) {
	var progressBar = dropzone.parent().find(".progressbar");
	var progressBarLine = progressBar.find(".line");
	var fileInput = dropzone.find(".inputFile");
	var inputId = fileInput.attr("name");
	dropzone.fileupload({
		dataType: "json",
		url: "/wfe/upload",
		fileInput: fileInput,
		done: function (e, data) {
			var statusText = progressBar.find(".statusText");
			var statusImg = progressBar.find(".statusImg");
			var label = data.result.name + "<span style='color: #888'> - " + data.result.size + "</span>";
			statusImg.attr("src", "/wfe/images/delete.png");
			statusImg.addClass("inputFileDelete");
			statusText.html("<a href='/wfe/upload?action=view&inputId=" + inputId + "'>" + label + "</a>");
		},
		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			progressBarLine.css("width", progress + "%");
		},
		dropZone: dropzone
	}).bind("fileuploadsubmit", function (e, data) {
		data.formData = {
			inputId: inputId
		};
		dropzone.hide();
		progressBar.show();
	}).bind('fileuploadfail', function (e, data) {
		var statusText = progressBar.find(".statusText");
		var statusImg = progressBar.find(".statusImg");
		statusImg.attr("src", "/wfe/images/error.gif");
		statusImg.addClass("inputFileDelete");
		statusText.html(data.textStatus);
	});
}

function deleteFile(inputId) {
	var dropzone = $("input[name='" + inputId + "']").parent();
	dropzone.show();
	jQuery.ajax({
		type: "GET",
		url: "/wfe/upload",
		data: {
			action: "delete", 
		    	inputId: inputId
		},
		dataType: "html",
		success: function(msg) {
			var progressBar = dropzone.parent().find(".progressbar");
			progressBar.hide();
			var statusText = progressBar.find(".statusText");
			statusText.html(loadingMessage);
			var statusImg = progressBar.find(".statusImg");
			statusImg.attr("src", "/wfe/images/loading.gif");
			statusImg.removeClass("inputFileDelete");
			var progressBarLine = progressBar.find(".line");
			progressBarLine.css("width", "0%");
		}
	});
}
