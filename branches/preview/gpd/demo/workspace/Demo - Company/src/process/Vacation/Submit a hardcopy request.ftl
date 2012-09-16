<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Submit a hardcopy of vacation request to the human resource department</STRONG></P>
<DIV><LABEL for="requester">Employee </LABEL>${DisplayActor("requester", "fullname")}</DIV>
<DIV><LABEL for="since">Since </LABEL>${since?date}</DIV>
<DIV><LABEL for="till">Till </LABEL>${till?date}</DIV>
<DIV><LABEL for="reason">Reason </LABEL>${reason}</DIV>
<DIV><LABEL>Employee Comments</LABEL>${comment?multiline}</DIV>
<DIV><LABEL>Boss comments </LABEL>${boss_comment?multiline}</DIV>
<DIV><LABEL>Human resource inspector comments </LABEL>${human_resource_inspector_comment?multiline}</DIV>
<DIV> </DIV>
</DIV>