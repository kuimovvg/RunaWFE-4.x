<%@ page language="java" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<tiles:put name="body" type="string">
<%
	String parameterName = "botID";
	long id = Long.parseLong(request.getParameter(parameterName));
	String returnAction="/bot.do?" + parameterName+ "=" +id;
%>
        <wf:botTag botID="<%= id %>"/>
        	<table width="100%">
                <tr>
                    <td align="left"><wf:saveBotLink href="<%="save_bot.do?id=" + id %>"/></td>
                </tr>
            </table>
        <wf:botTaskListTag botID="<%= id %>">
			<table width="100%">
                <tr>
                    <td align="left"><wf:addBotTaskLink href="<%="create_bot_task.do?id=" + id %>"/></td>
                </tr>
            </table>
        </wf:botTaskListTag>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>