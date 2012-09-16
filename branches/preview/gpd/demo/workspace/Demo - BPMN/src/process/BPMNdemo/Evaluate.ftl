<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Evaluate a request</STRONG></P>
<FIELDSET><LEGEND>Request</LEGEND>
<DIV><LABEL>Comment1</LABEL> ${comment1?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Decision</LEGEND>
<DIV><LABEL for="comment1">comment2 </LABEL> <TEXTAREA name="comment2" rows="4" wrap="hard"/></DIV>
<DIV class="controlset"><SPAN class="label"/><INPUT checked="checked" id="bossDecisionApprove" name="evaluateResult" type="radio" value="true"/> <LABEL for="bossDecisionApprove">Approve</LABEL>  <INPUT id="bossDecisionDisapprove" name="evaluateResult" type="radio" value="false"/> <LABEL for="bossDecisionDisapprove">Reject</LABEL> </DIV>
</FIELDSET></DIV>
<P> </P>