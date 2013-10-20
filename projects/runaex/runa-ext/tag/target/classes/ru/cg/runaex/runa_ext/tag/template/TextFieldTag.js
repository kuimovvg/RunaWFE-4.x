if (${speechAvailable}) {
  $('#' + ${fieldId} +'-speech').focus(function () {
    $('#' + ${fieldId} +'-speech').blur()
  });
  $('#' + ${fieldId} +'-speech').bind('webkitspeechchange', function () {
    $('#' + ${fieldId}).val($(this).val());
  });
  $('#' + ${fieldId}).change(function () {
    $('#' + ${fieldId} +'-speech').val($('#' + ${fieldId}).val());
  });
}
$(document).ready(function () {
  if (${hasMask}) {

    var mask =${mask};

//Если используется Маска то в случае денег используем plugin moneyMask в ином случае plugin mask
    var $el = $('#' +${fieldId});
    $el.attr('mask', true);
    if (mask === 'MONETARY_EXTENDED') {
      $el.maskMoney({thousands: ' ', decimal: ',', precision: 2});
      $el.maskMoney('mask');
      $el.attr('mask-type', 'MONETARY_EXTENDED');
    }
    else if (mask === 'MONETARY') {
      $el.maskMoney({thousands: ' ', precision: 0});
      $el.maskMoney('mask');
      $el.attr('mask-type', 'MONETARY');
    }
    else if (mask === 'NUMBER') {
      $el.maskMoney({thousands: '', precision: 0});
      $el.maskMoney('mask');
      $el.attr('mask-type', 'NUMBER');
    }
    else {
      $el.mask(mask);
    }
    $el.trigger('mask');
  }
});
