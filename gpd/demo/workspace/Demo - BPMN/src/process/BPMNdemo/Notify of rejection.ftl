<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Rejected</STRONG></P>
<FIELDSET><LEGEND>Request</LEGEND>
<DIV> </DIV>
<DIV><LABEL for="requester">Reguester</LABEL> ${DisplayActor("requester", "fullname")}</DIV>
<BR/>
<DIV><LABEL for="comment1">Comment1</LABEL>${comment1?multiline}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Comments</LEGEND>
<DIV><LABEL for="comment2">comment2</LABEL>${comment2?multiline}</DIV>
<DIV><LABEL for="comment3">comment3</LABEL>${comment3?multiline}</DIV>
</FIELDSET></DIV>
<P> </P>