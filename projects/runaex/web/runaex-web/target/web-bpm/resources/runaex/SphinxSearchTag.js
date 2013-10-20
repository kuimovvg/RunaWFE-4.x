/**
 * Created with IntelliJ IDEA.
 * User: iabdulin
 * Date: 30.01.13
 * Time: 16:48
 * To change this template use File | Settings | File Templates.
 */

function createSphinxSearchFields(field, objects, idValues, labels) {

  for (var key in idValues) {
    $('input.sphinx-search-hidden[sphinx-search-id=\"' + key + '\"].sphinx-search-hidden[element-id="' + field + '"]').val(idValues[key]);
  }
  $('#' + field + '+ div + table').remove();

  var innerHtml = '<table class=\"sphinx-search-generated\">';

  for (var key in objects) {
    innerHtml = innerHtml + '<tr><td>' + labels[key] + '</td><td><input type=\"text\" value=\"' + objects[key] + '\" disabled=\"disabled\"</td></tr>'
  }
  innerHtml = innerHtml + '</table>'
  $('#' + field).parent().append(innerHtml);

}

function removeSphinxSearchFields(field) {
  $('input.sphinx-search-hidden[element-id="' + field + '"]').val(null);
  $('#' + field + '+ div + table').remove();
}