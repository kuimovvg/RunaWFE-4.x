CKEDITOR.plugins.add('runaExTabs',
    {
      init: function (editor) {

        editor.ui.addButton('InsertTab',
            {
              label: 'Insert Tab',
              command: 'insertTab',
              icon: this.path + 'images/insertTab.gif'
            });

        editor.ui.addButton('DeleteTab',
            {
              label: 'Delete Tab',
              command: 'DeleteTab',
              icon: this.path + 'images/deleteTab.gif'
            });

        editor.addCommand('DeleteTab',
            {
              exec: function (editor) {
                var sel = editor.getSelection();
                if (sel != null) {
                  var selectionStartEl = editor.getSelection().getStartElement();
                  var currEl = selectionStartEl;
                  while (currEl != null && currEl.$.getAttribute('name') != "cg-tab-content") {
                    currEl = currEl.getParent();
                  }
                  if (currEl != null) {
                    var ulElement = currEl.getChildren().getItem(0);
                    var divElement = currEl.getChildren().getItem(1);

                    if (ulElement.getChildren().$.length > 1) {

                      for (var index = 0; index < ulElement.getChildren().$.length; index++) {
                        var htmlLiElement = ulElement.getChildren().getItem(index).$;

                        if (htmlLiElement.getAttribute('class') != null && htmlLiElement.getAttribute('class').indexOf('active') != -1) {
                          ulElement.getChildren().getItem(index).remove();
                          divElement.getChildren().getItem(index).remove();
                          break;
                        }
                      }
//                      ulElement.getChildren().getItem(ulElement.getChildren().$.length - 1).remove();
//                      divElement.getChildren().getItem(divElement.getChildren().$.length - 1).remove();
                    }
                    else {
                      currEl.remove();
                    }

                    editor.setData(editor.getData());
                    editor.updateElement();
                  }
                }
              }
            });


        editor.addCommand('insertTab',
            {
              exec: function (editor) {
                var sel = editor.getSelection();
                if (sel != null) {
                  var tabDivId = (new Date()).getTime();
                  var tabsDivId = tabDivId + 1;

                  var selectionStartEl = editor.getSelection().getStartElement();
                  var currEl = selectionStartEl;
                  while (currEl != null && (currEl.$.getAttribute('class') == null || currEl.$.getAttribute('class').indexOf("tab-pane") == -1)) {
                    if (currEl.$.getAttribute('name') == "cg-tab-content") {
                      currEl = null;
                      break;
                    }
                    currEl = currEl.getParent();
                  }

                  var isInTab = currEl != null;

                  currEl = selectionStartEl;
                  while (currEl != null && currEl.$.getAttribute('name') != "cg-tab-content") {
                    currEl = currEl.getParent();
                  }

                  if (currEl != null && !isInTab) {
                    var ulElement = currEl.getChildren().getItem(0);
                    var liElementHtml = "<li><a href=\"#tab-id-" + tabDivId + "\" data-toggle=\"tab\">Наименование вкладки</a></li>";
                    ulElement.appendHtml(liElementHtml);

                    var divElement = currEl.getChildren().getItem(1);
                    var divElementHtml = "<div class=\"tab-pane\" id=\"tab-id-" + tabDivId + "\">" +
                        "<p>Область для ввода</p>" +
                        "</div>";
                    divElement.appendHtml(divElementHtml);
                  }
                  else {
                    var html = "<div id=\"content-id-" + tabsDivId + "\" name=\"cg-tab-content\">" +
                        "<ul class=\"nav nav-tabs\" data-tabs=\"tabs\" id=\"tabs-id-" + tabsDivId + "\">" +
                        "<li class=\"active\"><a href=\"#tab-id-" + tabDivId + "\" data-toggle=\"tab\">Наименование вкладки</a></li>" +
                        "</ul>" +
                        "<div class=\"tab-content\" id=\"my-tab-content-id-" + tabsDivId + "\">" +
                        "<div class=\"tab-pane active\" id=\"tab-id-" + tabDivId + "\">" +
                        "<p>Область для ввода</p>" +
                        "</div>" +
                        "</div>" +
                        "</div>";
                    editor.insertHtml(html);
                  }
                  editor.setData(editor.getData());
                  editor.updateElement();
                }
              }
            });
      }
    });