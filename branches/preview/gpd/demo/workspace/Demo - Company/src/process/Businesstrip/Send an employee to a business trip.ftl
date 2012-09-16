<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Send an employee to a business trip</STRONG></P>
<DIV><LABEL for="staffrole">Employee <EM>*</EM></LABEL>${GroupMembers("staffrole", "staff", "all")}</DIV>
<DIV><LABEL for="since">Since <EM>*</EM></LABEL>${InputDateTime("since", "date")}<EM><FONT size="-1">(dd.mm.yyyy)</FONT></EM></DIV>
<DIV><LABEL for="till">Till <EM>*</EM></LABEL>${InputDateTime("till", "date")}<EM><FONT size="-1">(dd.mm.yyyy)</FONT></EM></DIV>
<DIV><LABEL for="reason">Business trip type <EM>*</EM></LABEL><SELECT id="businessTripType" name="businessTripType">
<OPTION selected="selected" value="local">local</OPTION>
<OPTION value="toAnotherRegion">to another region</OPTION>
</SELECT></DIV>
<DIV><LABEL for="reason">Reason <EM>*</EM></LABEL><INPUT id="reason" name="reason" type="text"/></DIV>
<DIV><LABEL for="comment">Comments</LABEL><TEXTAREA id="comment" name="comment"/></DIV>
</DIV>