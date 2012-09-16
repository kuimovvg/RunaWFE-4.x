<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.af.web.form.CreateExecutorForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ page import="ru.runa.commons.IOCommons" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
	<b><%= IOCommons.getLogDirPath() %></b>
	<ul>
		<li>server.log</li>
		<li>boot.log</li>
	</ul>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>