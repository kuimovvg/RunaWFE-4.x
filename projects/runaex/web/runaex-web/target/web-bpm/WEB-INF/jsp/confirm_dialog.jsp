<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

  <script type="text/javascript">
    function showConfirmDialog(msg, onConfirm) {
      var confirmDialog = $("#confirmDialog");
      var msgEl = confirmDialog.find(".modal-body");
      msgEl.empty();
      msgEl.append(msg);
      $("#confirmYesBtn")[0].onclick = onConfirm;
      confirmDialog.modal("show");
    }
  </script>

  <div id="confirmDialog" class="modal hide fade in" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-header">
      <h3><fmt:message key="confirmTitle"/></h3>
    </div>
    <div class="modal-body">
    </div>
    <div class="modal-footer">
      <input id="confirmYesBtn" type="button" value="<fmt:message key="yesButton"/>" class="btn btn-primary"
             data-dismiss="modal" aria-hidden="true"/>
      <input id="confirmCancelBtn" type="button" value="<fmt:message key="cancelButton"/>" class="btn"
             data-dismiss="modal" aria-hidden="true"/>
    </div>
  </div>

</fmt:bundle>
