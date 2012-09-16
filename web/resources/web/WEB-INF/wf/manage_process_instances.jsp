<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_process_instances.do";
%>

<wf:listProcessesInstancesForm  batchPresentationId="listProcessesInstancesForm"  returnAction="<%= returnAction %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listProcessesInstancesForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listProcessesInstancesForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listProcessesInstancesForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>