<%--
  Created by IntelliJ IDEA.
  User: Golovlyev
  Date: 19.12.12
  Time: 9:29
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

  <script type="text/javascript">

    function getPercentage() {
      $.ajax({
        type: "GET",
        url: "statusGenerateDb",
        success: function (html) {
          alert(html);
          $('#status').html(html);
        }
      });
    }

    function startProgressBar() {
      setInterval("getPercentage()", 100);
      $("#upload").submit();
    }

    function disableSaveBtn() {
      if(!$('#logo-name').val())
        $('#save_btn').attr('disabled', '');
      else
        $('#save_btn').removeAttr('disabled');
    }

    function uploadLogotype(btn) {
      $(btn).addClass('button-logo').attr('disabled', '')
          .parent().addClass('button-progress').addClass('active');
      $('#uploadLogotypeId').submit();
    }

    function cancelButton() {
      window.location = '<%=request.getContextPath()%>/tasks';
    }
  </script>

  <form:form action="uploadLogotype" method="POST" id="uploadLogotypeId" enctype="multipart/form-data">
    <div class="h">
      <label for="logo-name"><fmt:message key="projectName"/></label>
      <c:choose>
        <c:when test="${logo != null}">
          <input type="text" name="logo" id="logo-name" maxlength="70" size="10" value="${logo}"
                 onkeyup="disableSaveBtn()"/>
        </c:when>
        <c:otherwise>
          <input type="text" name="logo" id="logo-name" maxlength="70" size="10"
                 value="<fmt:message key="defaultName"/>"
                 onkeyup="disableSaveBtn()"/>
        </c:otherwise>
      </c:choose>
    </div>

    <div id="addlogotype">
      <label for="file-label"><fmt:message key="logotype"/></label>
      <input type="file" accept="image/jpeg,image/png,image/gif,image/bmp" name="logotype" id="file-label"/>
    </div>
    <div class="indent-top">
      <p>
        <input type="button" id="save_btn" class="btn btn-primary" value="<fmt:message key="save"/>"
               onclick="uploadLogotype(this)"/>
        <input type="button" id="cancel_btn" class="btn btn-primary" value="<fmt:message key="cancel"/>"
               onclick="cancelButton()"/>
      </p>
    </div>
  </form:form>
</fmt:bundle>