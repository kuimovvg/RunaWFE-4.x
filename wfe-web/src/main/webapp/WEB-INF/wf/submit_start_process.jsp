<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.common.WebResources" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<% if (WebResources.isAjaxFileInputEnabled()) { %>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/taskformutils.js" />">c=0;</script>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/fileupload.css" />">
<% } %>
</tiles:put>

<tiles:put name="body" type="string">
<%
	String taskParameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(taskParameterName));
	String title = ru.runa.common.web.Commons.getMessage("title.start_form", pageContext);
%>

<wf:startForm title="<%= title %>" definitionId="<%= id %>" action="/submitStartProcessForm"/>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>