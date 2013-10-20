$(document).ready(function () {
  $('textarea.runaex').each(function (idx, el) {
    el = $(el);
    var td = el.closest('td');
    if (td[0]) {
      td.css('height', td.height());
      td.css('width', td.width());
      var height = td.innerHeight();
      var width = td.innerWidth();
      height -= getHeightMargins(td);
      width -= getWidthMargins(td);
      var parent = el;
      while (parent && !parent.is(td)) {
        height -= getHeightMargins(parent);
        width -= getWidthMargins(parent);
        parent = parent.parent();
      }

      el.css('height', height);
      el.css('width', width);
      el.css('max-height', height);
      el.css('max-width', width);
    }
    else {
      el.css('max-height', el.height());
      el.css('max-width', el.width());
    }
  });
});

function getHeightMargins(target) {
  return parseInt(target.css('margin-bottom')) + parseInt(target.css('margin-top'))
      + parseInt(target.css('padding-bottom')) + parseInt(target.css('padding-top'))
      + parseInt(target.css('border-bottom-width')) + parseInt(target.css('border-top-width'));
}

function getWidthMargins(target) {
  return parseInt(target.css('margin-left')) + parseInt(target.css('margin-right'))
      + parseInt(target.css('padding-left')) + parseInt(target.css('padding-right'))
      + parseInt(target.css('border-left-width')) + parseInt(target.css('border-right-width'));
}
