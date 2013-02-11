<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script>
	var storageVisible = false;
	var bottaskErrorsVisible = false;
	var processErrorsVisible = false;
	$(document).ready(function() {
		$("#storageButton").click(function() {
			if (storageVisible) {
				$("#storageContentDiv").hide();
				$("#storageImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				storageVisible = false;
			} else {
				$("#storageContentDiv").show();
				$("#storageImg").attr("src", "/wfe/images/view_setup_visible.gif");
				storageVisible = true;
			}
		});
		$("#bottaskErrorsButton").click(function() {
			if (bottaskErrorsVisible) {
				$("#bottaskErrorsContentDiv").hide();
				$("#bottaskErrorsImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				bottaskErrorsVisible = false;
			} else {
				$("#bottaskErrorsContentDiv").show();
				$("#bottaskErrorsImg").attr("src", "/wfe/images/view_setup_visible.gif");
				bottaskErrorsVisible = true;
			}
		});
		$("#processErrorsButton").click(function() {
			if (processErrorsVisible) {
				$("#processErrorsContentDiv").hide();
				$("#processErrorsImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				processErrorsVisible = false;
			} else {
				$("#processErrorsContentDiv").show();
				$("#processErrorsImg").attr("src", "/wfe/images/view_setup_visible.gif");
				processErrorsVisible = true;
			}
		});
		$("a[fileName]").each(function() {
			$(this).click(function() {
				editScript($(this).attr("fileName"), "<bean:message key="button.save" />", "<bean:message key="button.execute" />", "<bean:message key="button.cancel" />");
			});
		});
	});
	var saveSuccessMessage = "<bean:message key="adminkit.script.save.success" />";
	var executionSuccessMessage = "<bean:message key="adminkit.script.execution.success" />";
	var executionFailedMessage = "<bean:message key="adminkit.script.execution.failed" />";
	var buttonCloseMessage = "<bean:message key="button.close" />";
	</script>
	<script type="text/javascript" src="<html:rewrite page="/js/xmleditor/codemirror.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/scripteditor.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/errorviewer.js" />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string" >
<%
	String substitutionCriteriaIds = "";
	if (request.getParameter("substitutionCriteriaIds") != null) {
	    substitutionCriteriaIds = request.getParameter("substitutionCriteriaIds");
	}
%>

<wf:updatePermissionsOnSystemForm>
	<table width="100%">
	<tr>
		<td align="left">
			<wf:grantLoginPermissionOnSystemLink  />
		</td>
		<td align="right">
			<wf:showSystemLogLink href='<%= "/show_system_logs.do" %>'/>
		</td>
	</tr>
	</table>
</wf:updatePermissionsOnSystemForm>

<wf:listSubstitutionCriteriasForm buttonAlignment="right" substitutionCriteriaIds="<%= substitutionCriteriaIds %>">
	<table width="100%">
	<tr>
		<td align="left">
			<wf:addSubstitutionCriteriaLink  />
		</td>
	</tr>
	</table>
</wf:listSubstitutionCriteriasForm>

<table class='box'><tr><th class='box'><bean:message key="adminkit.scripts" /></th></tr>
<tr><td class='box'>
	<div style="position: relative;">
		<div style="position: absolute; right: 5px; top: 5px;">
			<table><tbody><tr>
				<td class="hidableblock">
					<a id="storageButton" href="javascript:void(0)" class="link">
						<img id="storageImg" class="hidableblock" src="/wfe/images/view_setup_hidden.gif">
						&nbsp;<bean:message key="adminkit.savedscripts" />
					</a>
				</td>
			</tr></tbody></table>
		</div>
		<div>
			<table>
				<tr>
					<td class='hidableblock'>
						<a href="javascript:void(0)" class='link' onclick='javascript:uploadScript("<bean:message key="button.save" />", "<bean:message key="button.execute" />", "<bean:message key="button.cancel" />");'><bean:message key="button.upload" /></a>
						&nbsp;&nbsp;&nbsp;
						<a href="javascript:void(0)" class='link' onclick='javascript:editScript("", "<bean:message key="button.save" />", "<bean:message key="button.execute" />", "<bean:message key="button.cancel" />");'><bean:message key="button.create" /></a>
					</td>
				</tr>
				<tr>
					<td>
					</td>
				</tr>
			</table>
		</div>
		<div id="storageContentDiv" style="display: none;">
			<wf:viewAdminkitScripts />
		</div>
	</div>
</td></tr></table>

<table class='box'><tr><th class='box'><bean:message key="title.errors" /></th></tr>
<tr><td class='box'>
	<div>
		<a id="bottaskErrorsButton" href="javascript:void(0)" class="link">
			<img id="bottaskErrorsImg" class="hidableblock" src="/wfe/images/view_setup_hidden.gif">
			&nbsp;<bean:message key="errors.bottask" />
		</a>
	</div>
	<div id="bottaskErrorsContentDiv" style="display: none;">
		<wf:viewBotTaskErrors />
	</div>
	<br />
	<div>
		<a id="processErrorsButton" href="javascript:void(0)" class="link">
			<img id="processErrorsImg" class="hidableblock" src="/wfe/images/view_setup_hidden.gif">
			&nbsp;<bean:message key="errors.process" />
		</a>
	</div>
	<div id="processErrorsContentDiv" style="display: none;">
		<wf:viewProcessErrors />
	</div>
</td></tr></table>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>