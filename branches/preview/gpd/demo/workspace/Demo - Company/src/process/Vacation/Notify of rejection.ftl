<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Vacation is rejected</STRONG></P>
<FIELDSET><LEGEND>Your request</LEGEND>
<DIV><LABEL for="requester">Employee </LABEL>${DisplayActor("requester", "fullname")}</DIV>
<DIV><LABEL for="since">Since </LABEL>${since?date}</DIV>
<DIV><LABEL for="till">Till </LABEL>${till?date}</DIV>
<DIV><LABEL for="reason">Reason </LABEL>${reason}</DIV>
<DIV><LABEL for="comment">Comments</LABEL>${comment?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Comments</LEGEND>
<DIV><LABEL for="boss comment">Boss comments </LABEL>${boss_comment?multiline}</DIV>
<DIV><LABEL for="human resource inspector comment">Human resource inspector comments </LABEL>${human_resource_inspector_comment?multiline}</DIV>
</FIELDSET></DIV>