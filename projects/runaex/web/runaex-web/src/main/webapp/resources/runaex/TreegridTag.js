/*
 * Copyright (c) 2012.
 *
 * Class: TreeGridTag.js
 * Last modified: 25.09.12 14:16
 *
 * Author: Sabirov
 * Company Center
 */

function onSelectTreeItemClickHandler(data) {
  var url = 'selectedTreeItem';
  $.post(url, data, function (success) {
    if (success.indexOf("error") != -1) {
      success = success.replace("error:","");
      $("#ajax-error").removeClass("hidden").text(success);
    }
  });
}