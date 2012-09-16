<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_tasks.do";
%>
<wf:listTasksForm batchPresentationId="listTasksForm" buttonAlignment="right" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listTasksForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listTasksForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listTasksForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
<tiles:put name="head" type="string">
	<meta http-equiv="refresh" content="180; URL='<html:rewrite action="/manage_tasks.do?tabForwardName=manage_tasks"/>'">
</tiles:put>
</tiles:insert>