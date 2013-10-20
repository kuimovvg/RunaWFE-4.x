$(document).ready(function () {
  resizeContainer();
  var gridWidth = Math.floor(computeFreeWidth(${widthWeight}, 'flexigrid-${gridElementId}', ${isPaginationVisible}));
  var gridHeight = Math.floor(computeFreeHeight(${heightWeight}, 'flexigrid-${gridElementId}',true));
  $('#flexigrid-${gridElementId}').flexigrid({
    url: 'flexigrid?s=' + ${schema} +'&t=' + ${table} +'&dependent=' +${isDependent} + '&processDefinitionId=' + getProcessDefinitionId(),
    dataType: 'json',
    colModel: ${colModel},
    pClearFilter: ${pClearFilter},
    sortname: ${sortname},
    sortorder: ${sortorder},
    usepager: ${isPaginationVisible},
    useRp: true,
    rp: ${defaultElementsCountOnPage},
    showTableToggleBtn: false,
    singleSelect: true,
    width: gridWidth,
    height: gridHeight,
    resizable: false,
    onRowClick:${onRowClickHandler},
    params: ${params}})
});