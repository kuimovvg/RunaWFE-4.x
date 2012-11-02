<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
    String relationName = request.getParameter("relationName");
    long relationId = Long.parseLong(request.getParameter("id"));
	String returnAction = "/grant_permission_on_relation.do?id=" + relationId + "&relationName=" + relationName;
%>
<wf:listExecutorsWithoutPermissionsOnRelationForm batchPresentationId="listExecutorsWithoutPermissionsOnRelationsForm" returnAction="<%= returnAction %>" relationName="<%= relationName %>" identifiableId="<%= relationId %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutPermissionsOnRelationsForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutPermissionsOnRelationsForm"  returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listExecutorsWithoutPermissionsOnRelationForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>