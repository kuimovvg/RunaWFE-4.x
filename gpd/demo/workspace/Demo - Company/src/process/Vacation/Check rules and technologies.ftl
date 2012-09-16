<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Check that all rules and technologies for vacation request are correct</STRONG></P>
<FIELDSET><LEGEND>Request</LEGEND>
<DIV><LABEL for="requester">Employee </LABEL>${DisplayActor("requester", "fullname")}</DIV>
<DIV><LABEL for="since">Since </LABEL>${since?date}</DIV>
<DIV><LABEL for="till">Till </LABEL>${till?date}</DIV>
<DIV><LABEL for="reason">Reason </LABEL>${reason}</DIV>
<DIV><LABEL>Employee Comments</LABEL>${comment?multiline}</DIV>
<DIV><LABEL for="boss comment">Boss comments </LABEL>${boss_comment?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Your decision</LEGEND>
<DIV><LABEL for="comment">Comments </LABEL> <TEXTAREA name="human_resource_inspector_comment"/></DIV>
<DIV class="controlset"><SPAN class="label"/><INPUT checked="checked" id="ok" name="humanResourceInspectorCheckResult" type="radio" value="true"/> <LABEL for="ok">Correct</LABEL>  <INPUT id="no" name="humanResourceInspectorCheckResult" type="radio" value="false"/> <LABEL for="no">Not correct</LABEL> </DIV>
</FIELDSET></DIV>