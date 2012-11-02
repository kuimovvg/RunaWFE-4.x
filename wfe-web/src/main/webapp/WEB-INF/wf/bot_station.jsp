<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<tiles:put name="body" type="string">
<%
	String parameterName = "botStationID";
	long id = Long.parseLong(request.getParameter(parameterName));
	String returnAction="/bot_station.do?" + parameterName+ "=" +id;
%>
        <wf:botStationTag botStationID="<%= id %>"/>
        <table width="100%">
            <tr>
                <td align="left"><wf:saveBotStationLink href="<%="save_bot_station.do?id=" + id %>"/></td>
            </tr>
        </table>
        <wf:botStationStatusTag botStationID="<%= id %>"/>
        <wf:deployBot ID="<%= id %>"/>
        <wf:botListTag botStationID="<%= id %>">
			<table width="100%">
                <tr>
                    <td align="left"><wf:createBotLink href="<%="add_bot.do?botStationID=" + id %>"/></td>
                </tr>
            </table>
        </wf:botListTag>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>