<%@ page language="java" pageEncoding="UTF-8" %>
<%
	response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
	response.setHeader("Location", "/wfe");
%>
