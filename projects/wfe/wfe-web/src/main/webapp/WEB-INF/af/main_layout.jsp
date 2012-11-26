<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="af" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.common.WebResources" %>
<%@ page import="ru.runa.common.Version" %>

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
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/main.css" />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/jquery-ui-1.9.2.custom.css" />">
	<script type="text/javascript" src="<html:rewrite page="/js/jquery-1.8.3.min.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery-ui-1.9.2.custom.min.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery-ui-i18n.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.mask.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.timepicker.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.timepicker-i18n.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/common.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.cookie.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/confirm.js" />">c=0;</script>
	<tiles:insert attribute="head" ignore="true"/>
  </head>
<body>
<% if (thinInterface == null || !thinInterface.equals("true")) { %>
	<table class="box">
		<tr>
			<td width="15%">
				<a href="http://wf.runa.ru/About" target="new">
					<img hspace="10" border="0" src="<html:rewrite page="/images/big_logo.png"/>" alt="Runa WFE">
				</a>
			</td>
			<td width="85%" >
				<table width="100%">	
				<tr> 
					<td align="left" >
						<tiles:insert attribute="messages"/>
					</td>
					<td align="right">
						 <af:loginAsMessage message="<%= Commons.getMessage(\"label.logged_as\", pageContext) %>" /><br><af:logout />
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
				<%= WebResources.getAdditionalLinks() %>
				<% if (Version.isDisplay()) { %>
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
