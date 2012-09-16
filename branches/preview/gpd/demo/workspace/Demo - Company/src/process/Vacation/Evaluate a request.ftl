<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Evaluate a vacation request</STRONG></P>
<FIELDSET><LEGEND>User request</LEGEND>
<DIV><LABEL for="requester">Employee </LABEL>${DisplayActor("requester", "fullname")}</DIV>
<DIV><LABEL for="since">Since </LABEL>${since?date}</DIV>
<DIV><LABEL for="till">Till </LABEL>${till?date}</DIV>
<DIV><LABEL for="reason">Reason </LABEL>${reason}</DIV>
<DIV><LABEL>Comments</LABEL>${comment?multiline}</DIV>
<DIV><LABEL for="comment">Human resource inspector comments </LABEL>${human_resource_inspector_comment?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Your decision</LEGEND>
<DIV><LABEL for="comment">Boss comments </LABEL> <TEXTAREA name="boss_comment" rows="4" wrap="hard"/></DIV>
<DIV class="controlset"><SPAN class="label"/><INPUT checked="checked" id="bossDecisionApprove" name="bossDecision" type="radio" value="true"/> <LABEL for="bossDecisionApprove">Approve</LABEL>  <INPUT id="bossDecisionDisapprove" name="bossDecision" type="radio" value="false"/> <LABEL for="bossDecisionDisapprove">Reject</LABEL> </DIV>
</FIELDSET></DIV>
<P> </P>