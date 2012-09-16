<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Overtime work is rejected</STRONG></P>
<DIV><LABEL for="manager">Manager </LABEL>${DisplayActor("manager", "fullname")}</DIV>
<DIV><LABEL for="staff">Employee (staff) </LABEL>${DisplayActor("staffrole", "fullname")}</DIV>
<DIV><LABEL for="since">Since </LABEL>${since?datetime}</DIV>
<DIV><LABEL for="till">Till </LABEL>${till?datetime}</DIV>
<DIV><LABEL for="reason">Reason </LABEL>${reason}</DIV>
<DIV><LABEL>Comments</LABEL>${comment?multiline}</DIV>
<DIV><LABEL>Employee comments </LABEL>${staff_person_comment?multiline}</DIV>
</DIV>
<P> </P>