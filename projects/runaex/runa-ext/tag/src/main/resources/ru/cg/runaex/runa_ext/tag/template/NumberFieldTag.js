$(document).ready(function () {
  var mask =${mask};

//Если используется Маска то в случае денег используем plugin moneyMask в ином случае plugin mask
var $el = $('#' +${fieldId});
  $el.attr('mask', true);
  if (mask === 'MONETARY_EXTENDED') {
    $el.maskMoney({thousands: ' ', decimal: ',', precision: 2, allowNegative: true, allowZero: true});
    $el.attr('mask-type', 'MONETARY_EXTENDED');
  }
  else if (mask === 'MONETARY') {
    $el.maskMoney({thousands: ' ', precision: 0, allowNegative: true, allowZero: true});
    $el.attr('mask-type', 'MONETARY');
  }
  else if (mask === 'NUMBER') {
    $el.maskMoney({thousands: '', precision: 0, allowNegative: true, allowZero: true});
    $el.attr('mask-type', 'NUMBER');
  }
  else {
    $el.mask(mask);
  }
  $el.trigger('mask');
});