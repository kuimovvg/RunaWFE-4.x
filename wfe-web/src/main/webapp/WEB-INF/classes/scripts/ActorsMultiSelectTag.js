
var numCounter_VARIABLE = 0;
$(document).ready(function() {
    $('#btnAdd_VARIABLE').click(function() {
        updateDialogContent_VARIABLE();
    });
    $.editDialog_VARIABLE = $('<div></div>').dialog({
        width: 300, modal:true, autoOpen: false, 
        overlay: {backgroundColor: '#000', opacity: 0.5}
    });
    $.editDialog_VARIABLE.html("<input id='dialogFilter_VARIABLE' style='width: 100%;'><div id='dialogContent_VARIABLE' style='height: 300px; overflow-y: scroll;'></div>");
    $("#dialogFilter_VARIABLE").change(function() {
        updateDialogContent_VARIABLE();
    });
    $("#dialogFilter_VARIABLE").keyup(function() {
        updateDialogContent_VARIABLE();
    });
});

function updateDialogContent_VARIABLE() {
    $("#dialogContent_VARIABLE").html("");
    $.getJSON(
        "jsonUrl",
        {tag: "ActorsMultiSelect", qualifier: "VARIABLE", hint: $("#dialogFilter_VARIABLE").val()},
        function(data) {
            $.each(data, function(i, item) {
                $("#dialogContent_VARIABLE").append("<div><a href='javascript:addActor_VARIABLE(\""+item.id+"\", \""+escapeQuotesForHtmlContext(item.name)+"\");'>"+item.name+"</a></div>");
            });
        }
    );
    $.editDialog_VARIABLE.dialog('open');
}

function addActor_VARIABLE(id, name) {
    numCounter_VARIABLE++;
    var divId = "div_VARIABLE" + numCounter_VARIABLE;
    var e = "<div id='" + divId + "'>";
    e += "<input type='hidden' name='VARIABLE' value='"+id+"' /> " + name;
    e += " <a href='javascript:{}' onclick='$(\"#"+divId+"\").remove();'>[ X ]</a>";
    e += "</div>";
    $('#actorsMultiSelectCnt_VARIABLE').append(e);
    $.editDialog_VARIABLE.dialog("close");
}