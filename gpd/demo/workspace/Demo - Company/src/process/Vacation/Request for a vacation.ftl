<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Request for a vacation</STRONG></P>
<DIV><LABEL for="since">Since <EM>*</EM></LABEL>${InputDateTime("since", "date")}<EM><FONT size="-1"> (dd.mm.yyyy)</FONT></EM></DIV>
<DIV><LABEL for="till">Till <EM>*</EM></LABEL>${InputDateTime("till", "date")}<EM><FONT size="-1"> (dd.mm.yyyy)</FONT></EM></DIV>
<DIV><LABEL for="reason">Reason <EM>*</EM></LABEL><INPUT id="reason" name="reason" type="text"/></DIV>
<DIV><LABEL for="comment">Comments</LABEL><TEXTAREA id="comment" name="comment" wrap="hard"/></DIV>
</DIV>