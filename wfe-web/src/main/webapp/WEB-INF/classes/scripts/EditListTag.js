
$(document).ready(function() {
    var numCounterVARIABLE = $('.clonedVARIABLE').length;
    $('#btnAddVARIABLE').click(function() {
        numCounterVARIABLE++;
        var divId = "divVARIABLE" + numCounterVARIABLE;
        var e = "<div id='" + divId + "' style='margin-bottom:4px;' class='clonedVARIABLE'>";
        e += "COMPONENT_INPUT";
        e += "<input type='button' value=' - ' onclick='$(\"#"+divId+"\").remove();' />";
        e += "</div>";
        $('#btnAddVARIABLE').before(e);
        COMPONENT_JS_HANDLER
    });
});
