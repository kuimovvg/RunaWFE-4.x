
var numCounterVARIABLE = 0;
$(document).ready(function() {
    $('#btnAddVARIABLE').click(function() {
        updateDialogContent();
    });
    $.editDialog = $('<div></div>').dialog({
        width: 300, modal:true, autoOpen: false, 
        overlay: {backgroundColor: '#000', opacity: 0.5}
    });
    $.editDialog.html("<input id='dialogFilter' style='width: 100%;'><div id='dialogContent' style='height: 300px; overflow-y: scroll;'></div>");
    $("#dialogFilter").change(function() {
        updateDialogContent();
    });
    $("#dialogFilter").keyup(function() {
        updateDialogContent();
    });
});

function updateDialogContent() {
    $("#dialogContent").html("");
    $.getJSON(
        "jsonUrl",
        {tag: "ActorsMultiSelect", qualifier: "VARIABLE", hint: $("#dialogFilter").val()},
        function(data) {
            $.each(data, function(i, item) {
                $("#dialogContent").append("<div><a href='javascript:addActor(\""+item.code+"\", \""+item.name+"\");'>"+item.name+"</a></div>");
            });
        }
    );
    $.editDialog.dialog('open');
}

function addActor(code, name) {
    numCounterVARIABLE++;
    var divId = "divVARIABLE" + numCounterVARIABLE;
    var e = "<div id='" + divId + "'>";
    e += "<input type='hidden' name='VARIABLE' value='"+code+"' /> " + name;
    e += " <a href='javascript:{}' onclick='$(\"#"+divId+"\").remove();'>[ X ]</a>";
    e += "</div>";
    $('#actorsMultiSelectCntVARIABLE').append(e);
    $.editDialog.dialog("close");
}