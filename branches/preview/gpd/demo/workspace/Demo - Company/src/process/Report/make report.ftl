<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Make report</STRONG></P>
<FIELDSET><LEGEND>Report request</LEGEND>
<DIV><LABEL for="manager">Manager </LABEL>${DisplayActor("manager", "fullname")}</DIV>
<DIV><LABEL for="report_theme">Report theme </LABEL>${report_theme}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Report content</LEGEND>
<DIV><TEXTAREA name="report" style="width: 100%"/></DIV>
</FIELDSET></DIV>