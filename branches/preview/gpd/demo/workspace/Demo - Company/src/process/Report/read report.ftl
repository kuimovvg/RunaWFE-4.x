<P>
<LINK href="form.css" rel="stylesheet" type="text/css"/></P>
<DIV class="form-container">
<P class="legend"><STRONG>Read report</STRONG></P>
<FIELDSET><LEGEND>Report info</LEGEND>
<DIV><LABEL for="manager">Manager </LABEL>${DisplayActor("manager", "fullname")}</DIV>
<DIV><LABEL for="staff">Report maker </LABEL>${DisplayActor("staff", "fullname")}</DIV>
<DIV><LABEL for="report_theme">Report theme </LABEL>${report_theme}</DIV>
</FIELDSET> <FIELDSET><LEGEND>Report content</LEGEND>
<DIV>${report?multiline}</DIV>
</FIELDSET></DIV>