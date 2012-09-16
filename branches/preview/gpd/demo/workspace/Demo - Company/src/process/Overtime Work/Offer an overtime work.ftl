<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Offer an overtime work</STRONG></P>
<DIV><LABEL for="staff">Employee <EM>*</EM></LABEL>${GroupMembers("staffrole", "staff", "all")}</DIV>
<DIV><LABEL for="since">Since <EM>*</EM></LABEL><EM><FONT size="-1">${InputDateTime("since", "datetime")} (dd.mm.yyyy)</FONT></EM></DIV>
<DIV><LABEL for="till">Till <EM>*</EM></LABEL><EM><FONT size="-1">${InputDateTime("till", "datetime")} (dd.mm.yyyy)</FONT></EM></DIV>
<DIV><LABEL for="reason">Reason <EM>*</EM></LABEL><INPUT id="reason" name="reason" type="text"/></DIV>
<DIV><LABEL for="comment">Comments</LABEL><TEXTAREA id="comment" name="comment" wrap="hard"/></DIV>
</DIV>