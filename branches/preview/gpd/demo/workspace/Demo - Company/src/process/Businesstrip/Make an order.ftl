<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Make a business trip official order</STRONG></P>
<FIELDSET><LEGEND>User request</LEGEND>
<DIV><LABEL>Boss </LABEL>${DisplayActor("boss", "fullname")}</DIV>
<DIV><LABEL>Employee </LABEL>${DisplayActor("staffrole", "fullname")}</DIV>
<DIV><LABEL>Since </LABEL>${since?date}</DIV>
<DIV><LABEL>Till </LABEL>${till?date}</DIV>
<DIV><LABEL>Business trip type </LABEL>${businessTripType}</DIV>
<DIV><LABEL>Reason </LABEL>${reason}</DIV>
<DIV><LABEL>Comments</LABEL>${comment?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Order info</LEGEND>
<DIV><LABEL>Official order number </LABEL><INPUT name="official_order_number" type="text"/></DIV>
<DIV><LABEL>Official order date </LABEL>${InputDateTime("official_order_date", "date")}</DIV>
</FIELDSET></DIV>
<P> </P>