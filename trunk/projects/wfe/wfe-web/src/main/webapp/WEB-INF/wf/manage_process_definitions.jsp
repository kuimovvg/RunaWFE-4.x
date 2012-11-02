<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">

<%
	String returnAction = "/manage_process_definitions.do";
%>

<wf:listProcessesDefinitionsForm  batchPresentationId="listProcessesDefinitionsForm" buttonAlignment="right" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listProcessesDefinitionsForm" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listProcessesDefinitionsForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<wf:deployDefinitionLink forward="deploy_definition" /> 
</wf:listProcessesDefinitionsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>