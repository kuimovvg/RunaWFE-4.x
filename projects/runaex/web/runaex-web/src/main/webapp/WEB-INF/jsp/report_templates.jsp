<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.report_jsp_messages">

  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/resources/stylesheets/flexigrid.pack.css">
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/resources/stylesheets/runaex.flexigrid.css">
  <script src="<%=request.getContextPath()%>/resources/javascripts/flexigrid.pack.js" type="text/javascript"></script>
  <script src="<%=request.getContextPath()%>/resources/runaex/FlexiGridCommon.js" type="text/javascript"></script>
  <script type="text/javascript">

    $(document).ready(function () {
      var gridHeight = Math.floor(getTotalHeight());
      $("#templatesGrid").flexigrid({
        url: '<%=request.getContextPath()%>/load-templates',
        dataType: 'json',
        colModel: [
          {display: '<fmt:message key="templatesName"/>', name: 'report_template_name', width: $('#form').width() - 48, sortable: true, align: 'left'}
        ],
        sortname: "name",
        sortorder: "asc",
        usepager: true,
        useRp: true,
        resizable: false,
        onRowClick: onTemplateSelect,
        onSuccess: onTemplatesLoad,
        height: gridHeight
      });
    });

    function onTemplatesLoad() {
      $("#deleteTemplateBtn").attr('disabled', '');
    }

    function onTemplateSelect(src) {
      var selected = $(src).hasClass("trSelected");

      if (selected) {
        $("#deleteTemplateBtn").removeAttr('disabled');
      }
      else {
        $("#deleteTemplateBtn").attr('disabled', '');
      }
    }

    function deleteTemplates() {
      blockMainUI();
      var templateIds = [];
      $("#templatesGrid").find('.trSelected').each(function () {
        var id = this.id.replace('row', '');
        templateIds.push(id);
      });

      if (templateIds.length > 0) {
        $.ajax({
          url: '<%=request.getContextPath()%>/delete-templates/',
          type: 'POST',
          processData: false,
          data: "templateIds=" + templateIds,
          success: function (response) {
            $("#templatesGrid").flexReload();
            $("#deleteTemplateBtn").attr('disabled', '');
            $(".section").prepend('<div id ="temp_alert" class="alert alert-autohide" style="margin-bottom: 28px;">' + response.autohide + '</div>');
            var $temp_alert = $("#temp_alert");
            $temp_alert.alert();
            setTimeout(function () {
              $temp_alert.alert('close');
              $temp_alert.remove();
            }, 2000);
          },
          complete: function (qXHR, textStatus) {
            unblockMainUI();
          }
        });
      }
    }

  </script>

  <div id="form">
    <div id="templates" style="width: 99%; display: inline-block;">
      <h1 class="formHeader" style="margin: 0 -5px 20px -5px"><fmt:message key="templatesHeader"/></h1>

      <div>
        <input id="addTemplateBtn" type="button" class="btn" value="<fmt:message key="addTemplateBtn"/>"
               onclick="js:window.location.href='<%=request.getContextPath()%>/uploadReportTemplates'"/>
        <input id="deleteTemplateBtn" type="button" class="btn" value="<fmt:message key="deleteTemplateBtn"/>"
               onclick="showConfirmDialog('<fmt:message key="deleteTemplateConfirmMsg"/>', deleteTemplates)" disabled/>
      </div>

      <div id="templatesGrid" height-weight="100" class="flexigrid-mark"></div>
    </div>
    <jsp:include page="confirm_dialog.jsp"/>
  </div>

</fmt:bundle>