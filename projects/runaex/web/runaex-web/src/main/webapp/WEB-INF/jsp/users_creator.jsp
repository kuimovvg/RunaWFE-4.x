<%--
  Created by IntelliJ IDEA.
  User: Golovlyev
  Date: 11.06.13
  Time: 11:06
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

  <script type="text/javascript">
    function addNewFile() {
      var ext = document.getElementById("file-label").value;
      var submitEl = document.getElementById('save_btn');
      submitEl.disabled = ext === "";
    }

    function uploadFile(btn) {
      $(btn).addClass('button-file').attr('disabled', '')
          .parent().addClass('button-progress').addClass('active');
      $('#createUsersId').submit();
    }

    function cancelButton(btn) {
      window.location = '<%=request.getContextPath()%>/tasks';
    }
  </script>

  <form:form action="generateUsers" method="POST" id="createUsersId" enctype="multipart/form-data">
    <div id="fileWithUsers">
      <h5><fmt:message key="browseFile"/></h5>
      <input type="file"
             accept=".csv"
             name="fileUploader" id="file-label"
             onchange="addNewFile()"/>
    </div>
    <div class="indent-top">
      <p>
        <input type="button" id="save_btn" class="btn btn-primary" value="<fmt:message key="endCreateUsers"/>"
               disabled="disabled"
               onclick="uploadFile(this)"/>
        <input type="button" id="cancel_btn" class="btn btn-primary" value="<fmt:message key="goBack"/>"
               onclick="cancelButton(this)"/>
      </p>
    </div>
  </form:form>
</fmt:bundle>