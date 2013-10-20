<%@ page import="ru.cg.runaex.validation.ErrorMessage" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedList" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">
<%
  String error = (String) request.getAttribute("error");
  if (error != null)
    request.setAttribute("error", error.replaceAll("\\n", "<br/>"));

  String info = (String) request.getAttribute("info");
  if (info == null || info.isEmpty())
    info = request.getParameter("info");
  if (info != null)
    request.setAttribute("info", info.replaceAll("\\n", "<br/>"));

  String autohide = (String) request.getAttribute("autohide");
  if (autohide == null || autohide.isEmpty())
    autohide = request.getParameter("autohide");
  if (autohide != null)
    request.setAttribute("autohide", autohide.replaceAll("\\n", "<br/>"));

  String success = (String) request.getAttribute("success");
  if (success == null || success.isEmpty())
    success = request.getParameter("success");
  if (success != null)
    request.setAttribute("success", success.replaceAll("\\n", "<br/>"));
%>


<!DOCTYPE html>

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title><c:choose><c:when test="${projectName != null}">${projectName}</c:when><c:otherwise><fmt:message
      key="defaultName"/></c:otherwise></c:choose></title>
  <link rel="stylesheet" type="text/css"
        href="<%=request.getContextPath()%>/resources/stylesheets/all-${project.version}.css">
  <script src="<%=request.getContextPath()%>/resources/javascripts/all-${project.version}.js"
          type="text/javascript"></script>
  <link rel="shortcut icon" type="image/png" href="<%=request.getContextPath()%>/resources/images/favicon.png">


  <script type="text/javascript">

    $(document).ready(function () {
      // Enable or leave the keys
      $('.slider').each(function () {
        if ($('li:last', this).width() + $('li:last', this).offset().left - $('li:first', this).offset().left > $('div', this).width()) {
          // enable the buttons
          $('button', this).css('display', 'inline');
          $('button.prev', this).css('visibility', 'hidden');
        }
      });

      $(".slider .next").click(function () {
        //Remove the exist selector
        //Set the width to the widest of either
        var $div = $('div', this.parentNode)
            , maxoffset = $('li:last', $div).width() + $('li:last', $div).offset().left - $('li:first', $div).offset().left - $div.width()
            , offset = Math.abs(parseInt($('ul', $div).css('marginLeft')))
            , diff = $div.width();

        if (offset >= maxoffset)
          return;
        else if (offset + diff >= maxoffset) {
          diff = maxoffset - offset + 20;
          // Hide this
          $(this).css('visibility', 'hidden');
        }
        // enable the other
        $('.prev', this.parentNode).css('visibility', 'visible');

        $("ul", $(this).parent()).animate({
          marginLeft: "-=" + diff
        }, 400, 'swing');
      });

      $(".slider .prev").click(function () {

        var offset = Math.abs(parseInt($('ul', this.parentNode).css('marginLeft')));
        var diff = $('div', this.parentNode).width();
        if (offset <= 0)
          return;
        else if (offset - diff <= 0) {
          $(this).css('visibility', 'hidden');
          diff = offset;
        }
        $('.next', this.parentNode).css('visibility', 'visible');

        $("ul", $(this).parent()).animate({
          marginLeft: '+=' + diff
        }, 400, 'swing');
      });

      <c:if test="${autohide != null && autohide != \"\"}">
      $('.alert-autohide').alert();
      setTimeout(function () {
        $('.alert-autohide').alert('close');
      }, 2000);
      </c:if>
    });

  </script>

</head>

<body>
<object id="cadesplugin" type="application/x-cades" class="hiddenObject"
        style="position:absolute; visibility:hidden; height:0; width:0;"></object>

<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>

      <a class="brand" href="<%=request.getContextPath()%>/tasks">
            <img class="inline" src="<%=request.getContextPath()%>/downloadLogo">
            <span>${projectName}</span>
      </a>
      <c:if test="${username != null && username != \"\"}">
        <form class="navbar-search pull-left" action="">
          <input id="filter-input" type="text" class="input-medium search-query">
        </form>
        <div class="nav-collapse collapse">
          <ul class="nav">

            <c:if test="${adminCapabilities}">
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <fmt:message key="administrationTitle"/>
                <b class="caret"></b>
              </a>
              <ul class="dropdown-menu">
                <li <c:if test="${content == \"manage_process_definition\"}">class="active"</c:if>">
                <a href="<%=request.getContextPath()%>/manageProcessDefinitions"><fmt:message
                    key="manageApplication"/></a>
            </li>
            <li <c:if test="${content == \"change_logotype\"}">class="active"</c:if>">
            <a href="<%=request.getContextPath()%>/changeLogotype"><fmt:message key="changeLogotype"/></a>
            </li>
            <li <c:if test="${content == \"report_templates.jsp\"}">class="active"</c:if>">
            <a href="<%=request.getContextPath()%>/reportTemplates"><fmt:message key="reportTemplate"/></a>
            </li>
          </ul>
          </li>
          </c:if>

          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">Создать <b class="caret"></b></a>
            <ul class="dropdown-menu">
              <c:forEach items="${processDefinitions}" var="definition">
                <li>
                  <a href="<%=request.getContextPath()%>/startProcessInstance?n=${definition.name}">${definition.name}</a>
                </li>
              </c:forEach>
            </ul>
          </li>
          <li <c:if test="${content == \"tasks.jsp\"}">class="active"</c:if>>
            <a href="<%=request.getContextPath()%>/tasks" class="dropdown dropdown-toggle">
              Задачи
            </a>
          </li>
          </ul>
          <ul class="nav pull-right">
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown">${username}<b class="caret"></b></a>
              <ul class="dropdown-menu">
                <li><a href="<%=request.getContextPath()%>/j_spring_security_logout">Выход</a></li>
              </ul>
            </li>
          </ul>
        </div>
      </c:if>
    </div>
  </div>
</div>

<div class="otherPages">
  <c:if test="${error == null || error == \"\"}">
    <div id="ajax-error" class="alert alert-error hidden"></div>
  </c:if>
  <c:if test="${error != null && error != \"\"}">
    <div id="ajax-error" class="alert alert-error">${error}</div>
  </c:if>

  <c:if test="${autohide != null && autohide != \"\"}">
    <div class="alert alert-autohide">${autohide}</div>
  </c:if>

  <c:if test="${success != null && success != \"\"}">
    <div class="alert alert-success">${success}</div>
  </c:if>
  <jsp:include page="${content}.jsp"/>
  <jsp:include page="alert.jsp"/>
</div>

<div class="clearfix"></div>
<div class="footer">
  <div class="container">
    &copy; Center group
  </div>
</div>

</body>
</html>

</fmt:bundle>

