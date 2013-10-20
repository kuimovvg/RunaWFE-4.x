<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

  <div id="errorAlert" class="modal hide fade in" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
    <div class="modal-header">
      <h3><fmt:message key="alertTitle"/></h3>
    </div>
    <div class="modal-body alert alert-error" style="margin-bottom: 0">
    </div>
    <div class="modal-footer" style="text-align: center">
      <input type="button" value="<fmt:message key="okButton"/>" class="btn btn-primary" data-dismiss="modal" aria-hidden="true" onclick="js:window.location.href='<%=request.getContextPath()%>'"/>
    </div>
  </div>

</fmt:bundle>