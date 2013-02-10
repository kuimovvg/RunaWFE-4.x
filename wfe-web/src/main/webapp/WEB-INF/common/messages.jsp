<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="af" %>
<%@page import="org.apache.struts.taglib.TagUtils"%>
<%@page import="org.apache.struts.action.ActionMessage"%>
<%@page import="ru.runa.common.web.Resources"%>
<%@page import="ru.runa.common.web.Commons"%>
<af:globalExceptions/>

<CENTER>
<%--Used for ajax errors displaying --%>
<div id="ajaxErrorsDiv" class="error" style="font-weight: bold;">
</div>

<%
	ActionMessage m = (ActionMessage) Commons.getSessionAttribute(request.getSession(), Resources.USER_MESSAGE_KEY);
	if (m != null) {
	    String formatted = TagUtils.getInstance().message( 
	            pageContext, null, null, m.getKey(), m.getValues());
	    out.write("<font class=\"error\"><BR>" + formatted + "</font>");
	    request.getSession().setAttribute(Resources.USER_MESSAGE_KEY, null);
	}
%>

<%--Used for error message displaying --%>
<logic:messagesPresent>
	<font class="error">
		<html:messages id="commonError" property="org.apache.struts.action.GLOBAL_MESSAGE">
			<BR>
			<B><bean:write name="commonError" /></B>
		</html:messages>
		<html:messages id="processError" property="processErrors">
			<BR>
			<B style="border: 1px solid gray;"><bean:write name="processError" /></B>
		</html:messages>
	</font>
</logic:messagesPresent>
</CENTER>