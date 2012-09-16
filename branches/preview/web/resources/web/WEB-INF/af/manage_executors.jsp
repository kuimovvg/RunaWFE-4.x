<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_executors.do";
%>
<wf:listAllExecutorsForm  batchPresentationId="listAllExecutorsForm" buttonAlignment="right"  returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listAllExecutorsForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listAllExecutorsForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<wf:createActorLink />
	&nbsp;&nbsp;&nbsp;	
	<wf:createGroupLink />
</wf:listAllExecutorsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>