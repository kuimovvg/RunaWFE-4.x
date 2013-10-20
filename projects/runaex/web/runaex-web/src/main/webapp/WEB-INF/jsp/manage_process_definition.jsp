<%@ page import="ru.cg.runaex.validation.ErrorMessage" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">
  <%
    String validate_error = null;
    List<ErrorMessage> validateErrors = (List<ErrorMessage>) request.getAttribute("validateErrors");
    if (validateErrors != null) {
      validate_error = buildValidationErrors(validateErrors).replaceAll("\\n", "<br/>");
    }
  %>

  <%!
    private String buildValidationErrors(List<ErrorMessage> errorMessages) {
      StringBuilder html = new StringBuilder();
      List<String> idList = new LinkedList<String>();
      for (ErrorMessage errorMessage : errorMessages) {
        String id = "";
        for (int i = 0; i < errorMessage.getParts().size(); i++) {
          id = id.concat(errorMessage.getParts().get(i).getMessage()).intern();
          if (idList.contains(id))
            continue;
          idList.add(id);
          html.append("<div style=\"margin-left:")
              .append(i * 15).append("px;\">");
          if (errorMessage.getParts().get(i).isErrorMessage()) {
            html.append(errorMessage.getParts().get(i).getMessage())
                .append("</div>");
          }
          else {
            html.append(errorMessage.getParts().get(i).getTitle())
                .append(" \"")
                .append(errorMessage.getParts().get(i).getMessage())
                .append("\":</div>");
          }
        }
      }
      return html.toString();
    }
  %>

<script type="text/javascript">

  function disablingBtn() {
    if ($('#parfile').val() == "")
      $('#load_btn>input').attr('disabled','disabled');
    else
      $('#load_btn>input').removeAttr('disabled');
  }

  function getPercentage() {
    $.ajax({
      type: "GET",
      url: "statusGenerateDb",
//        dataType: "html",
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

  function uploadParFiles(btn) {
    $(btn).addClass('button-bar').attr('disabled', '')
        .parent().addClass('button-progress').addClass('active');
    $('#uploadParForm').submit();
  }
</script>

<%=validate_error != null ? "<div id=\"ajax-error\" class=\"alert alert-error\">" + validate_error + "</div>" : ""%>

<form:form action="uploadFiles" method="POST" id="uploadParForm" enctype="multipart/form-data">
  <div id="status"></div>
  <div>
    <h5><fmt:message key="downloadParFileTitle"/></h5>
  </div>
  <div id="addparfile">
    <input id="parfile" type="file" accept=".wba" name="parfile" onchange="disablingBtn()"/>
  </div>
  <div class="indent-top" id="load_btn">
    <input id="uploadBtn" type="button" disabled="disabled" class="btn btn-primary" value="<fmt:message key="downLoad"/>" onclick="uploadParFiles(this)"/>
  </div>
</form:form>

</fmt:bundle>