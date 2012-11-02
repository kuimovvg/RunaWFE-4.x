<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String relationName = request.getParameter("relationName");
	long executorId = Long.parseLong(request.getParameter("executorId"));
	String returnAction = "/manage_executor_right_relation.do?relationName=" + relationName + "&executorId=" + executorId;
%>
<wf:listExecutorRightRelationMembersForm buttonAlignment="right" returnAction="<%= returnAction %>" relationName="<%= relationName %>" executorId="<%= executorId %>">
	<table width="100%">
		<tr>
			<td align="left">
				<wf:createRelationRightExecutorLink relationName="<%= relationName %>" executorId="<%= executorId %>"/>
			</td>
		</tr>
	</table>
</wf:listExecutorRightRelationMembersForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>