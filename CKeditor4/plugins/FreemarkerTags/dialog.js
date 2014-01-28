CKEDITOR.dialog.add("FreemarkerMethod", function(editor) {
	// CKEDITOR.dialog.definition
	return {
		title: 'Sample dialog',
		resizable: CKEDITOR.DIALOG_RESIZE_BOTH,
		minWidth: 390,
		minHeight: 130,
		contents: [ {
			id: 'tab1',
			label: 'Label',
			title: 'Title',
			expand: true,
			padding: 0,
			elements: [ {
				type: 'html',
				html: '<p>This is some sample HTML content.</p>'
			},
			{
				type: 'textarea',
				id: 'textareaId',
				rows: 4,
				cols: 40
			} ]
		} ],
		buttons: [ CKEDITOR.dialog.okButton, CKEDITOR.dialog.cancelButton ],
			onOk: function() {
				var textareaObj = this.getContentElement( 'tab1', 'textareaId' );
				alert("You have entered: " + textareaObj.getValue());
			}
		}
});