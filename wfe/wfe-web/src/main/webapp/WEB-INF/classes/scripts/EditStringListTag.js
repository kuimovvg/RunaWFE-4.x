
$(document).ready(function() {
    var numCounterVARIABLE = $('.clonedVARIABLE').length;
    $('#btnAddVARIABLE').click(function() {
        numCounterVARIABLE++;
        var inputId = "VARIABLE" + numCounterVARIABLE;
        var divId = "div" + inputId;
        var e = "<div id='" + divId + "' style='margin-bottom:4px;' class='clonedVARIABLE'>";
        e += "<input name='VARIABLE' id='" + inputId + "' class='VARIABLE' />";
        e += "<input type='button' value=' - ' onclick='$(\"#"+divId+"\").remove();' />";
        e += "</div>";
        $('#btnAddVARIABLE').before(e);
    });
});
