var changeHandler = function (event, ui) {
    var dependentFieldsIdInput = $('input.ac-hidden-input[related-field="' + ${schema} +'.' + ${table} +'.${field}"]');
    var dependentFieldsAcInput = $('input.ac-input[related-field="' + ${schema} +'.' + ${table} +'.${field}"]');
    if (ui.item) {
        var selectedItemId = parseInt(ui.item.id);
        $('input[type="hidden"][name="${field}"]').val(selectedItemId);
        dependentFieldsIdInput.attr('disabled', null);
        dependentFieldsAcInput.attr('disabled', null).attr('related-field-value', selectedItemId);
    }
    else {
        $('input[type="hidden"][name="${field}"]').val(null);
        dependentFieldsIdInput.val(null).attr('disabled', 'disabled');
        dependentFieldsAcInput.attr('related-field-value', null).val(null).attr('disabled', 'disabled');
        var dependentFieldHandler = dependentFieldsAcInput.autocomplete('option', 'change');
        if (dependentFieldHandler && dependentFieldHandler.call) {
            dependentFieldHandler.call(undefined, undefined, new Object());
        }
        if (dependentFieldsIdInput.length != 0) {
            dependentFieldsIdInput.valid();
        }
        if (dependentFieldsAcInput.length != 0) {
            dependentFieldsAcInput.valid();
        }
        $(this).val(null);
    }
    $('#${hiddenFieldId}[type=hidden]').valid();
};
var autocomplete_${fieldId} = $('#${fieldId}.ac-input').autocomplete(
    {
        minLength: ${varMinSymbols},
        delay: ${varQueryDelay},
        source: function (request, response) {
            $.ajax({
                url: 'autocomplete',
                type: 'POST',
                dataType: 'jsonp',
                data: {references: ${referenceStr},
                    query: request.term,
                    relatedTableColumn: $('#${fieldId}.ac-input').attr('related-table-column'),
                    relatedLinkTable: $('#${fieldId}.ac-input').attr('related-link-table'),
                    relatedFieldValue: $('#${fieldId}.ac-input').attr('related-field-value'),
                    sortorder: ${sortOrder},
                    processDefinitionId: getProcessDefinitionId()
                },
                complete: function (resp) {
                    if (resp.status == 200) {
                        var responseArray = $.parseJSON(resp.responseText);
                        if (responseArray.length) {
                            if(responseArray.length==1)
                                autocomplete_${fieldId}.val(responseArray[0].value);
                            response(responseArray);
                        }
                        else {
                            autocomplete_${fieldId}.val(null);
                        }
                    }
                },
                errorMsgHandler: function (msg) {
                    var labelTag = $('#${fieldId}.ac-input').siblings('label.error[for="${fieldId}"]').not('.valid');
                    if (labelTag) {
                        labelTag.text(msg);
                        labelTag.closest('.control-group').addClass('error');
                    }
                }
            });
        },
        select: changeHandler,
        change: changeHandler,
        open: function () {
            $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
        },
        close: function () {
            $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
        }
    });

if (${isSpeechInputEnabled}) {
    autocomplete_${fieldId}.bind('webkitspeechchange', function () {
        $(this).keydown();
    });
}

autocomplete_${fieldId}.bind("change", function() {
    $(this).keydown();
});
