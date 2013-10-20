function helpClickHandler(elem) {
  var helpPopoverOpen = $(elem).attr("is-help-popover-open");
  $(elem).popover({
    html: true,
    trigger: 'manual',
    content: function () {
      return $(elem).attr("help-text");
    }
  });
  if (helpPopoverOpen == 'false') {
    $(elem).popover('show');
    helpPopoverOpen = true;
  }
  else {
    $(elem).popover('hide');
    helpPopoverOpen = false;
  }
  elem.setAttribute("is-help-popover-open", helpPopoverOpen);
}