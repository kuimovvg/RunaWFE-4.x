<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Accept or decline the offering of overtime work</STRONG></P>
<FIELDSET><LEGEND>Proposition</LEGEND>
<DIV><LABEL for="manager">Manager </LABEL>${DisplayActor("manager", "fullname")}</DIV>
<DIV><LABEL for="staff">Employee (staff) </LABEL>${DisplayActor("staffrole", "fullname")}</DIV>
<DIV><LABEL for="since">Since </LABEL>${since?datetime}</DIV>
<DIV><LABEL for="till">Till </LABEL>${till?datetime}</DIV>
<DIV><LABEL for="reason">Reason</LABEL>${reason}</DIV>
<DIV><LABEL>Comments </LABEL>${comment?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Your comments</LEGEND>
<DIV><TEXTAREA name="staff_person_comment" style="width: 100%"/></DIV>
</FIELDSET></DIV>