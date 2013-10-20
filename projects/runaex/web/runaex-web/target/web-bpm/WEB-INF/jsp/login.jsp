<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

<div id="form">
  <h1><fmt:message key="authorization"/></h1>

  <c:if test="${param.error != null && param.error == 1}">
    <div class="alert alert-error">Введён неверный логин или пароль
    </div>
  </c:if>

  <form:form cssClass="form-inline" action="j_spring_security_check" method="POST" commandName="principal">

    <div class="clearfix " id="login_field">
      <form:label path="login"><fmt:message key="username"/></form:label>
      <div class="input">
        <form:input path="login" id="login" name="login" placeholder="username"/>
        <form:errors path="login" cssClass="help-inline"/>
        <span class="help-block"></span>
      </div>
    </div>

    <div class="clearfix " id="password_field">
      <form:label path="password"><fmt:message key="password"/></form:label>
      <div class="input">
        <form:password path="password" id="password" name="password" placeholder="password"/>
        <form:errors path="password" cssClass="help-inline"/>
        <span class="help-block"></span>
      </div>
    </div>

    <p><input type="submit" class="btn btn-primary" value="<fmt:message key="into"/>"></p>
  </form:form>
</div>

</fmt:bundle>