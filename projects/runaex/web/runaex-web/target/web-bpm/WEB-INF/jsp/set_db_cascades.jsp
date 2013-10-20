<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/resources/stylesheets/ui.dynatree.css">
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/resources/stylesheets/dynatree.css">
  <script type="text/javascript" src="<%=request.getContextPath()%>/resources/javascripts/jquery.cookie.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/resources/javascripts/jquery.dynatree.js"></script>
  <%@ page import="ru.cg.runaex.generatedb.bean.Table" %>

  <%!
    public String getTableId(Table table) {
      return table.getSchema().getName().concat(".").concat(table.getName());
    }
  %>

  <script type="text/javascript">
    function addNewParFile() {
      $("#addparfile").append("<br><input type=\"file\" name=\"parfile\" onchange=\"javascript:addNewParFile()\">");
    }

    function generateDb(btn) {
      $(btn).addClass('button-bar').attr('disabled', '')
          .parent().addClass('button-progress').addClass('active');
      $('#dbCascadeForm').submit();
    }

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

    var markingDependentTables = false;

    function markDependentTables(mainTableId, dtnode) {
      var dbTablesTree = $("#dbTablesTree");
      var dependentTables = getDependentTables(mainTableId);
      if (dependentTables.length == 0)
        return;
      var dialogBody = $("#confirmMarkDependent").find(".modal-body");
      dialogBody.empty();
      dialogBody.append("<ul>");
      var dialogBodyUl = dialogBody.find("ul");
      for (var i = 0; i < dependentTables.length; i += 1) {
        var dependent = dependentTables[i];
        dialogBodyUl.append('<li>' + dependent.table + ' (<fmt:message key="depend"/> ' + dependent.field + ' <fmt:message key="ot"/> ' + dependent.mainTable + ')</li>');
      }
      dialogBody.append("</ul>");

      $("#dependentCascadeCancel")[0].onclick = function () {
        dtnode.select(false);
      };
      $("#dependentCascadeYes")[0].onclick = function () {
        markingDependentTables = true;
        for (var i = 0; i < dependentTables.length; i += 1) {
          var dependent = dependentTables[i];
          dbTablesTree.dynatree("getTree").getNodeByKey(dependent.field).select();
        }
        markingDependentTables = false;
        $('#confirmMarkDependent').modal("hide");
      };

      $('#confirmMarkDependent').modal();
    }

    function getDependentTables(mainTableId) {
      var results = [];
      var dbTablesTree = $("#dbTablesTree");
      var dependentTables = dbTablesTree.find('input[references="' + mainTableId + '"]');
      for (var i = 0; i < dependentTables.length; i += 1) {
        var dependent = dependentTables[i];
        var fieldId = dependent.id.replace("chb-", "");
        var tableId = dependent.attributes.table.value;
        if (!dbTablesTree.dynatree("getTree").getNodeByKey(fieldId).isSelected())
          results.push({table: tableId, field: fieldId, mainTable: mainTableId});
        results.push.apply(results, getDependentTables(tableId));
      }
      return results;
    }


    $(function () {
      var dbTablesTree = $("#dbTablesTree");
      dbTablesTree.dynatree({
        persist: false,
        checkbox: true,
        selectMode: 2,
        activeVisible: true,
        onSelect: function (select, dtnode) {
          //Un/check real checkboxes recursively after selection
          document.getElementById("chb-" + dtnode.data.key).checked = select;
          if (select && !markingDependentTables)
            markDependentTables(dtnode.data.table, dtnode);
        },
        onClick: function (node, event) {
          // We should not toggle, if target was "checkbox", because this
          // would result in double-toggle (i.e. no toggle)
          if (node.getEventTargetType(event) == "title")
            node.toggleSelect();
        }
      });
      //Hide real checkboxes
      dbTablesTree.find(":checkbox").addClass("hidden");
      //Update real checkboxes according to selections
      $.map(dbTablesTree.dynatree("getTree").getSelectedNodes(),
          function (dtnode) {
            $("#chb-" + dtnode.data.key).attr("checked", true);
            dtnode.activate();
          });
    });
  </script>

  <div><fmt:message key="selectCascadeFields"/></div>

  <form:form action="generateDb" id="dbCascadeForm">
    <div id="dbTablesTree">
      <ul id="dbTablesTreeData" style="display:none;">
        <c:forEach items="${tables}" var="table">
          <spring:eval expression="table.hasReferences()" var="hasReferences"/>
          <c:if test="${hasReferences}">
            <spring:eval expression="table.getSchema().getName().concat(\".\").concat(table.getName())" var="tableId"/>
            <li id="${tableId}" name="${tableId}" class="folder"
                data="unselectable: true, hideCheckbox: true, expand: true">
                ${table.name} (${table.schema.name})
              <ul>
                <spring:eval expression="table.getSortedFields()" var="sortedFields"/>
                <c:forEach items="${sortedFields}" var="field">
                  <spring:eval expression="field.getReferences() != null" var="fieldHasReferences"/>
                  <c:if test="${fieldHasReferences}">
                    <spring:eval expression="\"${tableId}\".concat(\".\").concat(field.getName())" var="fieldId"/>
                    <spring:eval expression="field.getReferences().isCascadeDeletion()" var="cascadeDeletion"/>
                    <spring:eval
                        expression="field.getReferences().getRefTable().getSchema().getName().concat(\".\").concat(field.getReferences().getRefTable().getName())"
                        var="referenceName"/>

                    <li id="${fieldId}" name="${fieldId}"
                        data="select: ${cascadeDeletion}, table:'${tableId}'">

                      <input type="checkbox" id="chb-${fieldId}" name="cascadeFks"
                             value="${fieldId}" references="${referenceName}" table="${tableId}"
                             class="hidden" ${cascadeDeletion ? "checked" : null}>${field.name} <fmt:message
                        key="referenceOn"/> ${referenceName})
                    </li>
                  </c:if>
                </c:forEach>
              </ul>
            </li>
          </c:if>
        </c:forEach>
      </ul>
    </div>
    <div class="indent-top">
      <input type="button" class="btn btn-primary" value="<fmt:message key="endDownload"/>" onclick="generateDb(this)"/>
    </div>
  </form:form>

  <div id="confirmMarkDependent" class="modal hide fade in" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
       aria-hidden="true">
    <div class="modal-header">
      <h3><fmt:message key="dependentTables"/></h3>

      <div><fmt:message key="selectCascadeAnswer"/></div>
    </div>
    <div class="modal-body">
    </div>
    <div class="modal-footer">
      <input type="button" id="dependentCascadeCancel" value="<fmt:message key="cancel"/>" class="btn"
             data-dismiss="modal"
             aria-hidden="true"/>
      <input type="button" id="dependentCascadeNo" value="Нет" class="btn" data-dismiss="modal" aria-hidden="true"/>
      <input type="button" id="dependentCascadeYes" value="Да" class="btn btn-primary"/>
    </div>
  </div>

</fmt:bundle>
