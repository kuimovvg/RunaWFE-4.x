/**
 * Created with IntelliJ IDEA.
 * User: Golovlyev
 * Date: 05.08.13
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */

addStyle('categoryitems');
addStyle('subcategoryitems');

function buildInbox(taskFilter) {
  var formData = new FormData();
  formData.append('taskFilter', taskFilter);
  $.ajax({
    url: 'buildInbox',
    type: 'POST',
    data: formData,
    success: function (response) {
      var inbox = document.getElementById('inbox-menu');
      $(inbox).empty();
      inbox.innerHTML += response;
      initTreeMenu();
    },
    error: function (response) {
    },
    cache: false,
    contentType: false,
    processData: false
  });
}

function initTreeMenu() {
  ddaccordion.init({
    headerclass: "expandable",
    contentclass: "categoryitems",
    revealtype: "click",
    mouseoverdelay: 200,
    collapseprev: false,
    defaultexpanded: [0],
    onemustopen: false,
    animatedefault: false,
    persiststate: false,
    toggleclass: ["", "openheader"],
    togglehtml: ["prefix", "", ""],
    animatespeed: "fast",
    oninit: function (headers, expandedindices) {

    },
    onopenclose: function (header, index, state, isuseractivated) {

    }
  });

  ddaccordion.init({
    headerclass: "subexpandable",
    contentclass: "subcategoryitems",
    revealtype: "click",
    mouseoverdelay: 200,
    collapseprev: false,
    defaultexpanded: [],
    onemustopen: false,
    animatedefault: false,
    persiststate: false,
    toggleclass: ["opensubheader", "closedsubheader"],
    togglehtml: ["none", "", ""],
    animatespeed: "fast",
    oninit: function (headers, expandedindices) {

    },
    onopenclose: function (header, index, state, isuseractivated) {

    }
  });
}

function addStyle(contentclass) {
  document.write('<style type="text/css">\n');
  document.write('.' + contentclass + '{display: none}\n'); //generate CSS to hide contents
  document.write('a.hiddenajaxlink{display: none}\n'); //CSS class to hide ajax link
  document.write('<\/style>');
}

