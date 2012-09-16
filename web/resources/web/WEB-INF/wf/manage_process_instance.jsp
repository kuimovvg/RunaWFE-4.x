<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.TaskIdForm" %>
<%@ page import="ru.runa.wf.web.action.ShowGraphModeHelper" %>
<%@ page import="ru.runa.commons.system.CommonResources" %>

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >

<% if (CommonResources.getDiagramRefreshInterval() > 0) { %>
<script type="text/javascript">
$(window).load(function() {
  window.setInterval("Reload()", <%= CommonResources.getDiagramRefreshInterval() %>);
});
function Reload() { 
   var src = $("#graph").attr("src");
   var pos = src.indexOf('timestamp');
   if (pos >= 0) {
      src = src.substr(0, pos);
   } else {
      src = src + '&';
   }
   src = src + "timestamp=" + new Date().getTime();
   $("#graph").attr("src", src);
}  
</script>

<% } %>
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
	long taskId = 0;
	String taskIdString = request.getParameter(TaskIdForm.TASK_ID_INPUT_NAME);
	if (taskIdString != null) {
		taskId = Long.parseLong(taskIdString);
	}
	long childProcessId = 0;
	String childProcessIdString = request.getParameter("childProcessId");
	if (childProcessIdString != null) {
		childProcessId = Long.parseLong(childProcessIdString);
	}
	
	boolean graphMode = ShowGraphModeHelper.isShowGraphMode();
%>
<wf:processInstanceInfoForm  buttonAlignment="right" identifiableId='<%= id %>' taskId='<%= taskId %>'>
<table width="100%">
	<tr>
		<td align="right">
			<wf:updatePermissionsOnIdentifiableLink identifiableId='<%=id %>' href='<%= "/manage_process_instance_permissions.do?" + parameterName+ "=" + id %>'  />
		</td>
	<tr>
	<tr>
		<td align="right">
			<wf:showHistoryLink identifiableId='<%=id %>' href='<%= "/show_history.do?" + parameterName+ "=" + id %>'  />
		</td>
	</tr>
	<tr>
		<td align="right">
			<wf:showGraphHistoryLink identifiableId='<%=id %>' href='<%= "/show_graph_history.do?" + parameterName+ "=" + id %>'  />
		</td>
	</tr>
	
	<% if(graphMode) { %>
	<tr>
		<td align="right">
			<wf:showGraphInstanceLink identifiableId='<%=id %>' href='<%= "/show_graph_instance.do?" + parameterName+ "=" + id + "&taskId=" + taskId + "&childProcessId=" + childProcessId %>'  />
		</td>
	</tr>
	<% } %>
</table>
</wf:processInstanceInfoForm>

<wf:processActiveTaskMonitor identifiableId='<%= id %>'  />
<wf:processSwimlaneMonitor identifiableId='<%= id %>'  />
<wf:processVariableMonitor identifiableId='<%= id %>'  />
<% if(!graphMode) { %>
	<wf:instanceGraphForm identifiableId='<%= id %>' taskId='<%= taskId %>' childProcessId='<%= childProcessId %>'/>
<% } %>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>