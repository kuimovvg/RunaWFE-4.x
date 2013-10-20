<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

  <html>
  <head>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/resources/stylesheets/all-1.27.2.css">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>
      <fmt:message key="errorPageTitle"/>
    </title>
  </head>
  <body>

  <div class="wrapper">
    <div class="navbar navbar-inverse navbar-fixed-top navbar-flayer">
      <div class="navbar-inner">
        <div class="container">
          <a class="brand" href="<%=request.getContextPath()%>">
            <img class="inline" src="<%=request.getContextPath()%>/resources/images/logo.png">
            <span><fmt:message key="defaultName"/></span>
          </a>
        </div>
      </div>
    </div>
    <div class="container">
      <div class="section">
        <div id="form">
          <h1><fmt:message key="errorPageText"/></h1>
          <h4>${exception}</h4><br />
          <a href="#" onclick="js:window.location.href='<%=request.getContextPath()%>'"><fmt:message key="goToMainPage"/></a>
        </div>
      </div>
    </div>
    <div class="reserved"></div>
  </div>
  <div id="footer">
    <div class="container">
      &copy; 2012 <img src="<%=request.getContextPath()%>/resources/images/logo-center.png" alt="Center group"/>
      <a href="http://www.cg.ru">Center group</a>
    </div>
  </div>

  </body>
  </html>

</fmt:bundle>