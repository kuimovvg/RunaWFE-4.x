var startTime = new Date(${year}, ${month}, ${day}, ${hours}, ${minutes}, ${seconds});

var diff = (Date.now() - startTime.getTime()) / 1000;
var str = parserTimeBySecond(diff, ${pattern});
$('#' +${fieldId}).empty();
$('#' +${fieldId}).append('<div class="timer"></div>');
$('#' + ${fieldId} +' .timer').append(str);

$('#' +${fieldId}).everyTime(1000, function (i) {
  var sT = new Date(${year}, ${month}, ${day}, ${hours}, ${minutes}, ${seconds});
  var diff = (Date.now() - sT.getTime()) / 1000;
  var str = parserTimeBySecond(diff, ${pattern});
  $('#' +${fieldId}).empty();
  $('#' +${fieldId}).append('<div class="timer"></div>');
  $('#' + ${fieldId} +' .timer').append(str);
});