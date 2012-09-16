<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
<tiles:put name="body" type="string" >
<%
	String substitutionCriteriaIDs = "";
	if (request.getParameter("substitutionCriteriaIDs") != null) {
	    substitutionCriteriaIDs = request.getParameter("substitutionCriteriaIDs");
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

<wf:listSubstitutionCriteriasForm buttonAlignment="right" substitutionCriteriaIDs="<%= substitutionCriteriaIDs %>">
	<table width="100%">
	<tr>
		<td align="left">
			<wf:addSubstitutionCriteriaLink  />
		</td>
	</tr>
	</table>
</wf:listSubstitutionCriteriasForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>