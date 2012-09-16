<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Make an official order for vacation</STRONG></P>
<FIELDSET><LEGEND>Request</LEGEND>
<DIV><LABEL for="requester">Employee </LABEL>${DisplayActor("requester", "fullname")}</DIV>
<DIV><LABEL for="since">Since </LABEL>${since?date}</DIV>
<DIV><LABEL for="till">Till </LABEL>${till?date}</DIV>
<DIV><LABEL for="reason">Reason </LABEL>${reason}</DIV>
<DIV><LABEL>Employee Comments</LABEL>${comment?multiline}</DIV>
<DIV><LABEL>Boss comments </LABEL>${boss_comment?multiline}</DIV>
<DIV><LABEL>Human resource inspector comments </LABEL>${human_resource_inspector_comment?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Give to request an official order</LEGEND>
<DIV><LABEL for="on">Official order number <EM>*</EM></LABEL> <INPUT id="on" name="official order number" style="width: 400px" type="text"/></DIV>
<DIV><LABEL for="od">Official order date <EM>*</EM></LABEL>${InputDateTime("official order date", "date")}<EM><FONT size="-1"> (dd.mm.yyyy)</FONT></EM></DIV>
</FIELDSET></DIV>