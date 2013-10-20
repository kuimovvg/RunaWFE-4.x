//Speech Recognition API
var recognizing = false;
var recognition;
var input;

$(document).ready(function () {
  if ('webkitSpeechRecognition' in window) {
    recognition = new webkitSpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = false;

    startIfPossible();

    $(document).oneTime(800, function () {
      recognition.stop();
    });

    var $inputs = $('input[speech_recognition_element="true"], textarea[speech_recognition_element="true"], select[speech_recognition_element="true"]');
    $inputs.mouseup(function (e) {
      if (e.button == 2) {
        stopRecognize();
      }
    });

    $inputs.mousedown(function (e) {
      if (e.button == 2) {
        input = $(e.target);
        startRecognize(input);
      }
    });

    $inputs.keyup(function (e) {
      if (e.keyCode == 17) {
        stopRecognize();
      }
    });

    $inputs.keydown(function (e) {
      input = $(e.target);
      if (e.keyCode == 17) {
        startRecognize(input);
      }
      if (e.keyCode == 27) {
        input.val('');
      }
    });

    $inputs.focusout(function (e) {
      stopRecognize();
    });

    $inputs.live("contextmenu", function (e) {
      return false;
    });
  }
});

function startIfPossible() {
  try {
    recognition.start();
  }
  catch (ex) {
    stopRecognize();
  }
}

function startRecognize(field) {
  if (!recognizing && recognition != undefined) {
    recognition.lang = 'ru-RU';

    recognition.onstart = function () {
      field.css("background-color", "#72A4D2");
      recognizing = true;
    };

    recognition.onend = function () {
      recognizing = false;
      field.css("background-color", "white");
    };

    recognition.onerror = function (event) {
      recognizing = false;
      field.css("background-color", "white");
    };

    recognition.onresult = function (event) {
      field.addClass('valid').closest('.control-group').addClass('success');
      $(document).oneTime(500, function () {
        field.removeClass('valid').closest('.control-group').removeClass('success');
      });
      var transcript = '';
      for (var i = event.resultIndex; i < event.results.length; ++i) {
        if (event.results[i].isFinal) {
          transcript += event.results[i][0].transcript;
        }
      }

      if (field[0].tagName == 'SELECT') {
        $(field.children().attr('selected', null).filter(function () {
          return this.label.toLowerCase().indexOf(transcript.toLowerCase()) == 0;
        }).get(0)).attr('selected', '');
      }
      else
        field.val(transcript).trigger("change");
    };
    startIfPossible();
  }
}

function stopRecognize() {
  if (recognizing && recognition != undefined) {
    recognition.stop();
    if (input != undefined)
      input.trigger('change');
  }
}




