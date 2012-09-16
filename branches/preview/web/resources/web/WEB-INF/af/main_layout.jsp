<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="af" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.wf.web.MainPageResources" %>
<%@ page import="ru.runa.commons.Version" %>
<%@ page import="ru.runa.commons.IOCommons" %>

<% 
	String thinInterface = (String)request.getAttribute("runawfe.thin.interface");
	String thinInterfacePage = (String)request.getAttribute("runawfe.thin.interface.page");
	if (thinInterfacePage == null) {
		thinInterfacePage="/start.do";
	}
%>

<html:html lang="true">
  <head>
  	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
  	<meta http-equiv="Cache-Control" content="no-cache">
  	<meta http-equiv="Pragma" content="no-cache">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/main.css" />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/tooltip/tooltip.css" />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/ui/jquery-ui-1.7.2.custom.css" />">
	<script type="text/javascript" src="<html:rewrite page="/jquery-1.3.2.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/jquery.cookie.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/confirm/confirm.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/xml_editor/js/codemirror.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/xml_editor/editor.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/ui/jquery-ui-1.7.2.custom.min.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/common.js" />">c=0;</script>
	<tiles:insert attribute="head" ignore="true"/>
  </head>
<body>
<% if (thinInterface == null || !thinInterface.equals("true")) { %>
	<table class="box">
		<tr>
			<td width="15%">
				<a href="http://wf.runa.ru/About" target="new">
					<img hspace="10" border="0" src="<html:rewrite page="/images/big_logo.gif"/>" alt="Runa WFE">
				</a>
			</td>
			<td width="85%" >
				<table width="100%">	
				<tr> 
					<td align="left" >
						<tiles:insert attribute="messages"/>
					</td>
					<td align="right">
						 <af:loginAsMessage message="<%= Commons.getMessage("label.logged_as", pageContext) %>" /><br><af:logout />
					<td>
				</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td valign="top" height="100%" width="15%">
				<hr>
				<table class="box">	
					<tr>
						<th class='box'><bean:message key="title.menu"/></th>
					</tr>
				</table>
				<af:tabHeader />
				<hr>
				<%-- //uncoment following lines for WFDEMO 
				<a href="http://sourceforge.net" target="new"><img src="http://sourceforge.net/sflogo.php?group_id=125156&amp;type=1" border="0" alt="SourceForge.net Logo"/></a>
				<BR>
				<B>Feedback</B> 
				<A HREF="http://sourceforge.net/forum/?group_id=125156" target="new">forum</A>
				--%>
				<%= MainPageResources.getAdditionalLinks() %>
				<% if (Version.isDisplay()) {
					  String path = IOCommons.getLogDirPath();
					  if (path != null && path.length() > 0) { 
				%>
					<div style="padding: 3px;">
						<a href="logs.do" style="color: aaa;"><bean:message key="title.logs"/></a>
					</div>
				<%    } %>
					<div style="padding: 3px; color: aaa;">
						<bean:message key="title.version"/> <b><%= Version.get() %></b>
					</div>
				<% } %>
			</td>
			<td valign="top"   height="100%" width="85%">
				<hr>
					<tiles:insert attribute="body"/>
				<hr>
			</td>
		</tr>
	</table>
<% } else { %>
	<a href='<html:rewrite action="<%=thinInterfacePage%>"/>' class='link'>Show start page</a>
	<tiles:insert attribute="messages"/>
	<tiles:insert attribute="body"/>
<% } %>
</body>
</html:html>
