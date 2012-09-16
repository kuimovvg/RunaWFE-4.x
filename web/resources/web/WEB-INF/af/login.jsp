<%@ page language="java" pageEncoding="UTF-8" session="false" %>
<%@ page import="ru.runa.af.web.NTLMSupportResources" %>
<%@ page import="ru.runa.af.web.KrbSupportResources" %>
<%@ page import="java.net.URLDecoder" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<% 
	String userName = request.getParameter("login") == null ? "" : URLDecoder.decode(request.getParameter("login"), "utf-8");
	String userPwd = request.getParameter("password") == null ? "" : URLDecoder.decode(request.getParameter("password"), "utf-8");
%>

<html:html lang="true">
  <head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/main.css" />">
    <html:base />
  </head>
	<body>
	<center>
			<table height = "100%">
				<tr height = "5%">
					<td></td>
				</tr>
				<tr height = "65%">
					<td align="center">
					   <html:form action="/login">
						<table>
							<tr>
								<td  align="left" colspan="2" target="new">
									<a href="http://wf.runa.ru/About">
										<img  border="0" src="<html:rewrite page="/images/big_logo.gif"/>" alt="Runa WFE">
									</a>
								</td>
							</tr>
							<tr>			
								<td><bean:message key="login.page.login.message"/></td>
      							<% if (userName==null) { %>
	      							<td style="width:170px" ><html:text property="login"/></td>
								<% } else { %>	
									<td style="width:170px" ><input   type="text" name="login" value="<%= userName %>"></td>
								<% } %>							
				  			</tr>
							<tr>
		  						<td><bean:message key="login.page.password.message"/></td>
      							<% if (userPwd==null) { %>
    	      						<td style="width:170px"><input  type="password" name="password" value=""></td>
								<% } else { %>	
    	      						<td style="width:170px"><input  type="password" name="password" value="<%= userPwd %>"></td>
								<% } %>							
			  				</tr>
							<tr>
								<td>
									<html:submit>
										<bean:message key="login.page.login.button"/>
									</html:submit>
								</td>
							</tr>					
						</table>
						</html:form>
						<% if ( NTLMSupportResources.isNTLMSupported() ){ %>
						<table>
							<tr>
								<td>
									<html:link action="/ntlmlogin">
										<bean:message key="login.page.login.ntlm"/>
									</html:link> 	
								</td>
							</tr>
						</table>
						<% } %>
						<% if ( KrbSupportResources.isKrbSupported() ){ %>
						<table>
							<tr>
								<td>
									<html:link action="/krblogin">
										<bean:message key="login.page.login.ntlm"/>
									</html:link> 	
								</td>
							</tr>
						</table>
						<% } %>
						<jsp:include page="../common/messages.jsp" />
					</td>
				</tr>
				<tr>
					<td></td>
				</tr>
			</table>
		</center>
	</body>
</html:html>