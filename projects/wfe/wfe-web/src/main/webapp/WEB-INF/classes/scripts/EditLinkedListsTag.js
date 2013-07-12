
$(document).ready(function() {
    var numCounter = $('.cloned').length;
    $('#editLinkedListsButtonAdd').click(function() {
        numCounter++;
        var trId = "editLinkedLists" + numCounter;
        var e = "<tr id='" + trId + "' class='cloned'>";
        e += "ROW_TEMPLATE";
        e += "<td><input type='button' value=' - ' onclick='$(\"#"+trId+"\").remove();' /></td>";
        e += "</tr>";
        $('#editLinkedLists').append(e);
        JS_HANDLERS
    });
});
