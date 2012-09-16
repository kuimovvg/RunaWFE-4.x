<#--
START SNIPPET: supported-validators
Only the following validators are supported:
* required validator
* requiredstring validator
* stringlength validator
* regex validator
* email validator
* url validator
* int validator
* double validator
END SNIPPET: supported-validators
-->
<#if ((parameters.validate?default(false) == true) && (parameters.performValidation?default(false) == true))>
<script type="text/javascript">
/*
	function clearErrorMessages(form) {
	    $("[errorFor]").each(function() {
	    	$(this).remove();
	    });
	}
	
	function addError(field, errorText) {
		var fe = $(field);
	   	var toolTipId = fe.attr("name") + "_tt";

		var errorImg = $("<img src='/wfe/images/error.gif' errorFor='yes'>");
		errorImg.hover(function(event){ showTip(event, toolTipId) }, function(){ hideTip(toolTipId) });
		fe.append(errorImg);

		var toolTipDiv = $("<div id='"+toolTipId+"' class='field-hint' style='display: none;' errorFor='yes'>");
		fe.append(toolTipDiv);
	   	toolTipDiv.append("<div>"+errorText+"</div>");
	}
*/

    function validateForm_${parameters.id}() {
        form = document.getElementById("${parameters.id}");
        clearErrorMessages(form);

        var errors = false;
    <#list parameters.tagNames as tagName>
        <#list tag.getValidators("${tagName}") as validator>
        // field name: ${validator.fieldName}
        // validator name: ${validator.validatorType}
        if (form.elements['${validator.fieldName}']) {
            field = form.elements['${validator.fieldName}'];
            var error = "${validator.getMessage()}";
            <#if validator.validatorType = "required">
            if (field.value == "") {
                addError(field, error);
                errors = true;
            }
            <#elseif validator.validatorType = "requiredstring">
            if (field.value != null && (field.value == "" || field.value.replace(/^\s+|\s+$/g,"").length == 0)) {
                addError(field, error);
                errors = true;
            }
            <#elseif validator.validatorType = "stringlength">
            if (field.value != null) {
                var value = field.value;
                <#if validator.trim>
                    //trim field value
                    while (value.substring(0,1) == ' ')
                        value = value.substring(1, value.length);
                    while (value.substring(value.length-1, value.length) == ' ')
                        value = value.substring(0, value.length-1);
                </#if>
                if(value.length > 0 && (
                        (${validator.minLength} > -1 && value.length < ${validator.minLength}) ||
                        (${validator.maxLength} > -1 && value.length > ${validator.maxLength})
                    )) {
                    addError(field, error);
                    errors = true;
                }
            } 
            <#elseif validator.validatorType = "regex">
            if (field.value != null && !field.value.match("${validator.expression?js_string}")) {
                addError(field, error);
                errors = true;
            }
            <#elseif validator.validatorType = "number">
            if ((field.value != null) && (field.value != "")) {
            	if (isNaN(parseInt(field.value))) {
            		addError(field, error);
	                errors = true;
            	}
                if (<#if validator.min?exists>parseInt(field.value) <
                     ${validator.min}<#else>false</#if> ||
                        <#if validator.max?exists>parseInt(field.value) >
                           ${validator.max}<#else>false</#if>) {
                    addError(field, error);
                    errors = true;
                }
            }
            <#elseif validator.validatorType = "double">
            if (field.value != null) {
            	var dValue = field.value.replace(",", ".");
            	if (isNaN(parseFloat(dValue))) {
            		addError(field, error);
	                errors = true;
            	}
                var value = parseFloat(dValue);
                if (<#if validator.minInclusive?exists>value < ${validator.minInclusive}<#else>false</#if> ||
                        <#if validator.maxInclusive?exists>value > ${validator.maxInclusive}<#else>false</#if> ||
                        <#if validator.minExclusive?exists>value <= ${validator.minExclusive}<#else>false</#if> ||
                        <#if validator.maxExclusive?exists>value >= ${validator.maxExclusive}<#else>false</#if>) {
                    addError(field, error);
                    errors = true;
                }
            }
            </#if>
        }
        </#list>
    </#list>

        return !errors;
    }
</script>
</#if>
