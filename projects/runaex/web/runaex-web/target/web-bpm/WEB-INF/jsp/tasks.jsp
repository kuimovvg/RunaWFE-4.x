<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="ru.cg.runaex.web.messages.jspMessages">

  <script>
    var table;
    $(document).ready(function () {
      table = $('#tasks-table').bootstraptable({
        url: 'get-tasks',
        columns: [
          {header: '<fmt:message key="processNumber"/>', width: '5%', html: '<div style="text-align: left"><a href="<%=request.getContextPath()%>/task?id={id}&amp;name={name}&amp;pname={processDefName}">{number}</a></div>'},
          {header: '<fmt:message key="businessProcess"/>', width: '15%', html: '<div style="text-align: left">{processDefName}</div>'},
          {header: '<fmt:message key="taskName"/>', width: '25%', html: '<div style="text-align: left"><a href="<%=request.getContextPath()%>/task?id={id}&amp;name={name}&amp;pname={processDefName}">{name}</a></div>'},
          {header: '<fmt:message key="version"/>', width: '10%', html: '<div style="text-align: left">{version}</div>'},
          {header: '<fmt:message key="createDate"/>', width: '17%', html: '<div style="text-align: left">{creationDate}</div>'},
          {header: '<fmt:message key="limit"/>', width: '18%', html: '<div style="text-align: left">{deadlineDate}</div>'},
          {header: '', width: '10%', html: '<div style="text-align: left"><a href="<%=request.getContextPath()%>/task?id={id}&amp;name={name}&amp;pname={processDefName}">Начать задачу</a></div>'}
        ],
        currentPage: 0,
        paginationSize: 20,
        height: function (event) {
          return ($(window).height() - event.div.offset().top - 50) + 'px';
        },
        usePagination: false,
        loadingGif: '<%=request.getContextPath()%>/resources/images/ddaccordion/loading2.gif',
        emptyValue: 'Список пуст'
      });

      $('#action-add').click(function (event) {
      });
      var filter = $('#filter-input');
      table.addFilterField(filter, 'taskFilter')
      $('.section').removeClass('section');
      $('.otherPages').removeClass('otherPages');
      filter[0].style.display = 'inline';
      $('body').resize();
    });

    function addCategoryFilter(id) {
      table.clearInboxFilters();
      table.addFilter("category", id);
      table.refresh();
    }

    function addBusinessProcessFilter(name) {
      table.clearInboxFilters();
      table.addFilter("businessProcessName", name);
      table.refresh();
    }

    function refreshTable() {
      table.clearInboxFilters();
      table.refresh();
      document.getElementById("processName").innerHTML = '<fmt:message key="tasksList"/>' + '::';
    }
  </script>

  <head>
    <script src="<%=request.getContextPath()%>/resources/javascripts/jquery.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/resources/javascripts/bootstrap-dropdown.js"
            type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/resources/javascripts/bootstrap-scrollspy.js"
            type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/resources/javascripts/bootstrap-collapse.js"
            type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/resources/javascripts/scripts.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/resources/javascripts/ddaccordion.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/resources/runaex/InboxBuilder.js"
            type="text/javascript"></script>
    <link href="<%=request.getContextPath()%>/resources/runaex/NavigationTreeMenuTag.css" rel="stylesheet"
          type="text/css">
    <script src="<%=request.getContextPath()%>/resources/javascripts/all-1.27.2.js"
            type="text/javascript"></script>
  </head>

  <div class="data">
    <c:if test="${error != null && error != \"\"}">
      <div class="alert alert-error">
        <pre>${error}</pre>
      </div>
    </c:if>
  </div>
  <div class="container_left main">
    <div class="container_right">
      <div class="fix-line2"></div>
      <div class="left">
        <ul id="inbox-menu" class="nav nav-list menu categoryitems" style="display: block;"></ul>
      </div>
      <div class="right">
        <div class="content">
          <div class="title">
            <a id="processName" href="#"><fmt:message key="tasksList"/> ::</a>
            <span><fmt:message key="tasksToUse"/></span>
          </div>
          <ul class="nav nav-tabs">
            <li class="active">
              <a href="#">Все</a>
            </li>
          </ul>
          <div id="tasks-table" class="nav-container nav-container-top"></div>
        </div>
      </div>
    </div>
  </div>
</fmt:bundle>