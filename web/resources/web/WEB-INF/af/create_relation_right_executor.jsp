<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.af.web.form.CreateExecutorForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String relationName = request.getParameter("relationName");
	long executorId = Long.parseLong(request.getParameter("executorId"));
	String title = ru.runa.common.web.Messages.getMessage("title.create_relation", pageContext) + relationName;
%>
	<wf:box title='<%= title %>'>
		<wf:createRelationRightExecutorForm relationName="<%= relationName %>" executorId="<%= executorId %>"/>
	</wf:box>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>