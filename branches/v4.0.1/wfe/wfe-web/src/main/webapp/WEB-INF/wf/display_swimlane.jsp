<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.SwimlaneForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	long processId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
	String swimlaneName = request.getParameter(SwimlaneForm.SWIMLANE_NAME_INPUT_NAME);
	String title = ru.runa.common.web.Commons.getMessage("title.swimlane_assignment", pageContext);
%>

<wf:processSwimlaneAssignmentMonitor title="<%= title %>" identifiableId ="<%= processId %>" swimlaneName="<%= swimlaneName %>" action="" />
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>