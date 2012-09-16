<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
%>

<wf:updatePermissionsOnProcessInstanceForm identifiableId="<%= id %>" >
<table width="100%">
	<tr>
		<td align="left">
			<wf:grantReadPermissionOnProcessInstanceLink identifiableId='<%= id %>' href='<%= "/grant_read_permission_on_process_instance.do?" + parameterName + "=" + id  %>'  />
		</td>
		<td align="right">
			<wf:updateProcessInstanceLink identifiableId='<%=id %>' href='<%= "/manage_process_instance.do?" + parameterName+ "=" + id %>'  />
		</td>
	</tr>
</table>
	 
</wf:updatePermissionsOnProcessInstanceForm>


</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>