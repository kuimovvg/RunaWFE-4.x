<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.SwimlaneForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String instanceParameterName = IdForm.ID_INPUT_NAME;
	long instanceId = Long.parseLong(request.getParameter(instanceParameterName));
	String swimlaneParameterName = SwimlaneForm.SWIMLANE_ID_INPUT_NAME;
	long swimlaneId = Long.parseLong(request.getParameter(swimlaneParameterName));
	String title = ru.runa.common.web.Commons.getMessage("title.swimlane_assignment", pageContext);
%>

<wf:processSwimlaneAssignmentMonitor title="<%= title %>" identifiableId ="<%= instanceId %>" swimlaneId="<%= swimlaneId %>" action="" />
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>