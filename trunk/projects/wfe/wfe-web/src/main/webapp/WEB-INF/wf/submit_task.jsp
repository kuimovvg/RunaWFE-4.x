<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.ProcessForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	long taskId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
	long actorId =  Long.parseLong(request.getParameter(ProcessForm.ACTOR_ID_INPUT_NAME));
	String title = ru.runa.common.web.Commons.getMessage("title.task_form", pageContext);
%>
<wf:taskDetails batchPresentationId="listTasksForm" title="<%= title %>" taskId="<%= taskId %>" actorId="<%= actorId %>" buttonAlignment="right" action="/processTaskAssignment" returnAction="/submitTaskDispatcher.do"/>
<wf:taskForm title="<%= title %>" taskId="<%= taskId %>" actorId="<%= actorId %>" action="/submitTaskForm" />

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>