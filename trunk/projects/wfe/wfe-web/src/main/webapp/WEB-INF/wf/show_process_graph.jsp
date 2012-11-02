<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.TaskIdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	Long id = Long.parseLong(request.getParameter(parameterName));
	Long taskId = null;
	String taskIdString = request.getParameter(TaskIdForm.TASK_ID_INPUT_NAME);
	if (taskIdString != null) {
		taskId = Long.parseLong(taskIdString);
	}
	Long childProcessId = null;
	String childProcessIdString = request.getParameter("childProcessId");
	if (childProcessIdString != null) {
		childProcessId = Long.parseLong(childProcessIdString);
	}
%>

<wf:processGraphForm identifiableId='<%= id %>' taskId='<%= taskId %>' childProcessId='<%= childProcessId %>'>
	<table width="100%">
	<tr>
		<td align="right">
			<wf:manageProcessLink identifiableId='<%=id %>' href='<%= "/manage_process.do?" + parameterName+ "=" + id + "&taskId=" + taskId + "&childProcessId=" + childProcessId %>'  />
		</td>
	</tr>
</table>
</wf:processGraphForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>