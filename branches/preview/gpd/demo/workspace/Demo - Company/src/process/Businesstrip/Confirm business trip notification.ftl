<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Confirm the business trip notification</STRONG></P>
<FIELDSET><LEGEND>User request</LEGEND>
<DIV><LABEL>Boss </LABEL>${DisplayActor("boss", "fullname")}</DIV>
<DIV><LABEL>Employee </LABEL>${DisplayActor("staffrole", "fullname")}</DIV>
<DIV><LABEL>Since </LABEL>${since?date}</DIV>
<DIV><LABEL>Till </LABEL>${till?date}</DIV>
<DIV><LABEL>Business trip type </LABEL>${businessTripType}</DIV>
<DIV><LABEL>Reason </LABEL>${reason}</DIV>
<DIV><LABEL>Comments</LABEL>${comment?multiline}</DIV>
</FIELDSET></DIV>