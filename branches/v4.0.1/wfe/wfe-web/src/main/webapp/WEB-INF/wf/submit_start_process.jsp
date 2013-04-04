<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String taskParameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(taskParameterName));
	String title = ru.runa.common.web.Commons.getMessage("title.start_form", pageContext);
%>

<wf:startForm title="<%= title %>" definitionId="<%= id %>" action="/submitStartProcessForm"/>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>