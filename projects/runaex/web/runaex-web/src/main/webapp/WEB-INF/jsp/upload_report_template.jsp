<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.report_jsp_messages">

  <script type="text/javascript">

    function setButtonState() {

      var $input = $("input[type=\"file\"]")[0];
      var htmlSelect = "";
      var length = $input.files.length;
      if (length > 1) {
        htmlSelect += "<div class=\"control-group\" id = \"parent-template-select\">";
        htmlSelect += "<label class=\"control-label\" for=\"parent-template\">" + "<fmt:message key="selectParentTemplate"/>" + "</label>";
        htmlSelect += "<div class=\"controls\">";
        htmlSelect += "<select id=\"parent-template\" name=\"parent-template\" class=\"combobox\" style=\"width:auto; max-width: 350px;\">";


        htmlSelect += "<option value=\"-\">";
        htmlSelect += "</option>";

        for (var i = 0; i < length; i++) {
          var name = $input.files[i].name.split(".")[0];
          htmlSelect += "<option value=\"" + name + "\">";
          htmlSelect += name;
          htmlSelect += "</option>";
        }
        htmlSelect += "</select>";
        htmlSelect += "</div>";
        htmlSelect += "</div>";

        var $parentCombo = $("#parent-template-select");
        if ($parentCombo.length == 0) {
          $('#parent-template-header-div').after($(htmlSelect));
        }
        else {
          $parentCombo.replaceWith(htmlSelect);
        }
      }

      var $uploadBtn = $('#uploadBtn');
      if ($('#jasperfile').val() == "")
        $uploadBtn.attr('disabled', 'disabled');
      else
        $uploadBtn.removeAttr('disabled');
    }

    function uploadTemplateFiles(btn) {
      $(btn).addClass('button-bar').attr('disabled', '')
          .parent().addClass('button-progress').addClass('active');

      var formData = new FormData();
      var fields = $("input[type=\"file\"]");
      var fileKey;
      $.each(fields, function (index, field) {
        $.each(field.files, function (index, file) {
          formData.append(file.name, file);
        })
      });

      var parentTemplateName = $("#parent-template").val();
      if (parentTemplateName != "-" && parentTemplateName != undefined)
        formData.append("parentTemplateName", parentTemplateName);

      $.ajax({
        url: "<%=request.getContextPath()%>/uploadTemplateFiles",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
          var info = '';
          if (response.info) {
            info = "?info=" + response.info.replace(new RegExp("\n", "g"), "<br/>");
            location.href = "reportTemplates" + info;
          }
        },
        complete: function () {
          $(btn).removeClass('button-bar').removeAttr('disabled')
              .parent().removeClass('button-progress').removeClass('active');
        }
      });
    }
  </script>

  <form:form action="uploadTemplateFiles" method="POST" id="uploadJasperForm" enctype="multipart/form-data">
    <div id="status"></div>
    <div id="parent-template-header-div">
      <h5><fmt:message key="downloadTemplateTitle"/></h5>
    </div>
    <div id="addTemplateFile">
      <input id="jasperfile" type="file" accept=".jasper" name="jasperfile" multiple="multiple"
             onchange="javascript:setButtonState()"/>
    </div>
    <div class="indent-top" id="load_btn">
      <input id="uploadBtn" type="button" disabled="disabled" class="btn btn-primary"
             value="<fmt:message key="downLoad"/>" onclick="uploadTemplateFiles(this)"/>
    </div>
  </form:form>

</fmt:bundle>