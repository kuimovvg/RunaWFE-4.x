var CAPICOM_CURRENT_USER_STORE = 2;
var CAPICOM_MY_STORE = "My";
var CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED = 2;
var CAPICOM_CERTIFICATE_FIND_SUBJECT_NAME = 1;
var CADES_BES = 1;
var VALID_TO = "), действителен до ";
var NO_VALID_CERTIFICATES = "Не найдено ни одного достоверного сертификата!";
var NO_PLUGIN = "Для создания электронной подписи необходимо установить cadesplugin. Пожалуйста, установите (обновите) его или обратитесь в службу технической поддержки.";
var ERROR_DURING_SIGN_ABORT_BY_USER = "Ошибка при создании подписи. Действие отменено пользователем.";
var ERROR_DURING_FILE_SIGN = "Ошибка при создании подписи файла(ов).";
var SECRET_KEY_NOT_FOUND = "Не найден закрытый ключ!";
var ERROR_DURING_SIGN = "Не удалось создать подпись. Описание: ";
var CANNOT_CREATE_SIGN = "Не удалось проверить подпись.";
var CERT_WITH_NUM_NOT_FOUND = "Сертификат с номером ? не найден.";
var WAIT_UNTIL_SIGN_COMPLETE = "Формируется цифровая подпись. Подождите, пожалуйста.";
var NO_DATA_FOR_SIGN = "Нет данных для подписи";
var CERT_SERIAL_NUM = "Серийный номер сертификата";
var CHOOSE_CERT = "Выберите сертификат";
var CERT_NOT_FOUND = "Сертификат не найден.";
var SIGN_ABORT_BY_USER_ERROR_CODE = "0x8010006E";
var SECRET_KEY_NOT_FOUND_ERROR_CODE = "0x8009200B";
var CLASS_NOT_REGISTERED_ERROR_CODE = "0x80040154";
var REQUIRED_BUILD_VERSION = 984;

function createObject(name) {
  switch (navigator.appName) {
    case "Microsoft Internet Explorer":
      return new ActiveXObject(name);
    default:
      var cadesobject = document.getElementById("cadesplugin");
      return cadesobject.CreateObject(name);
  }
}

function getCertBySubjectName(certSubjectName) {
  var oStore = createObject("CAPICOM.Store");
  oStore.Open(CAPICOM_CURRENT_USER_STORE, CAPICOM_MY_STORE,
      CAPICOM_STORE_OPEN_MAXIMUM_ALLOWED);

  var oCertificates = oStore.Certificates.Find(
      CAPICOM_CERTIFICATE_FIND_SUBJECT_NAME, certSubjectName);
  if (oCertificates.Count == 0) {
    showAlert(CERT_NOT_FOUND);
    return;
  }
  var oCertificate = oCertificates.Item(1);
  oStore.Close();
  return oCertificate;
}

function signCreate(certSubjectName, sn, dataToSign) {

  var oCertificate = sn != null ? getCertBySn(sn) : getCertBySubjectName(certSubjectName);
  if (oCertificate == null) {
    showAlert(CERT_WITH_NUM_NOT_FOUND.replace('?', sn));
    return null;
  }

  var oSigner = createObject("CAdESCOM.CPSigner");
  oSigner.Certificate = oCertificate;

  var oSignedData = createObject("CAdESCOM.CadesSignedData");
  oSignedData.Content = dataToSign;

  try {
    var sSignedMessage = oSignedData.SignCades(oSigner, CADES_BES, true);
  }
  catch (e) {
    if (e.message.indexOf(SIGN_ABORT_BY_USER_ERROR_CODE) > -1) {
      showAlert(ERROR_DURING_SIGN_ABORT_BY_USER);
    }
    else {
      if (e.message.indexOf(SECRET_KEY_NOT_FOUND_ERROR_CODE) > -1) {
        showAlert(SECRET_KEY_NOT_FOUND);
      }
      else {
        showAlert(ERROR_DURING_SIGN + e.message);
      }
    }

    return null;
  }
  return sSignedMessage;
}

function verify(sSignedMessage, data) {
  var oSignedData = createObject("CAdESCOM.CadesSignedData");
  try {
    oSignedData.Content = data;
    oSignedData.VerifyCades(sSignedMessage, CADES_BES, true);
  }
  catch (err) {
    showAlert(CANNOT_CREATE_SIGN);
    return false;
  }

  return true;
}

function getCertBySn(SerialNumber) {
  var oStore = createObject("CAPICOM.Store");
  oStore.Open(CAPICOM_CURRENT_USER_STORE);
  for (var i = 1; i <= oStore.Certificates.Count; i++) {
    var oCert = oStore.Certificates.Item(i);
    var serialNumberForCheck = SerialNumber;
    if (oCert.SerialNumber.length > serialNumberForCheck.length) {
      while (oCert.SerialNumber.length > serialNumberForCheck.length) {
        serialNumberForCheck = additionalLack(serialNumberForCheck);
      }
    }
    if (oCert.SerialNumber == serialNumberForCheck) {
      oStore.Close();
      return oCert
    }
  }
  oStore.Close();
  return null;
}

function additionalLack(b) {
  return '0' + b;
}

function checkForPlugIn() {
  var installed = false;
  switch (navigator.appName) {
    case 'Microsoft Internet Explorer':
      try {
        var obj = new ActiveXObject("CAdESCOM.CPSigner");
        installed = true;
      }
      catch (err) {
      }
      break;

    default:
      var mimetype = navigator.mimeTypes["application/x-cades"];
      if (mimetype) {
        var plugin = mimetype.enabledPlugin;
        if (plugin) {
          installed = true;
        }
      }
  }
  var minBuildVersionInstalled = true;
  try {
    var about = createObject("CADESCOM.About");
    minBuildVersionInstalled = about.BuildVersion >= REQUIRED_BUILD_VERSION;
  }
  catch (e) {
    if (e.message.indexOf(CLASS_NOT_REGISTERED_ERROR_CODE) > -1) {
      minBuildVersionInstalled = false;
    }
  }
  installed = minBuildVersionInstalled;

  return installed;
}

function signDataAndSend(data, objForSign, certSerialNumber, elem, extractedData) {
  var formData = extractedData.formData;
  var filesAndFileSignForDeletion = extractedData.filesAndFileSignForDeletion;

  if (Object.getOwnPropertyNames(objForSign).length > 0) {
    var dataForSign = JSON.stringify(objForSign);
    var signData = signCreate(null, certSerialNumber, dataForSign);

    if (signData == null)
      return;

    var fieldData = {
      "schema": $(elem).attr("data-schema"),
      "table": $(elem).attr("data-table"),
      "column": $(elem).attr("data-field"),
      "value": dataForSign,
      "tableId": $(elem).attr("data-tableId")
    };
    data.push(fieldData);

    fieldData = {
      "schema": $(elem).attr("data-schema"),
      "table": $(elem).attr("data-table"),
      "column": $(elem).attr("sign-field"),
      "value": signData,
      "tableId": $(elem).attr("data-tableId")
    };
    data.push(fieldData);
  }

  var taskId = document.getElementsByName("id")[0].value;
  var taskName = document.getElementsByName("taskName")[0].value;
  var processInstanceId = document.getElementsByName("processInstanceId")[0].value;
  var processName = document.getElementsByName("processName")[0].value;

  formData.append('action', "save");
  formData.append('data', JSON.stringify(data));
  formData.append('processInstanceId', processInstanceId);
  formData.append('processName', processName);
  formData.append('taskId', taskId);
  formData.append('taskName', taskName);
  formData.append('filesAndFileSignForDeletion', filesAndFileSignForDeletion);

  $.ajax({
    url: 'handleActionButton',
    type: 'POST',
    data: formData,
    success: function (response) {
      if (!response.noTasksAvailableInProcess) {
        var success = '';
        if (response.success)
          success = "&autohide=" + response.success;
        location.href = "task.htm?id=" + response.id + "&name=" + response.name + "&pname=" + response.pname + success;
      }
      else
        location.href = "tasks.htm";
    },
    complete: function (qXHR, textStatus) {
      unblockMainUI();
    },
    error: function () {
      elem.disabled = false;
    },
    cache: false,
    contentType: false,
    processData: false
  });
}

function saveData(certSerialNumber, elem, extractedData) {
  blockMainUI(WAIT_UNTIL_SIGN_COMPLETE);
  var data = extractedData.data;
  var formObjForSign = {};
  /*
   * generate JSON For Sign
   */
  var fields;
  fields = $("input.runaex[type=\"number\"][data-schema]");
  for (var i = 0; i < fields.length; i++) {
    formObjForSign[$(fields[i]).attr("name")] = $(fields[i]).val();
  }
  //  Текстовые поля
  fields = $("input.runaex[type=\"text\"][data-schema]:not(.ac-input)");
  for (i = 0; i < fields.length; i++) {
    formObjForSign[$(fields[i]).attr("name")] = $(fields[i]).val();
  }

  //  Комбобоксы
  fields = $("select.runaex[data-schema]");
  for (i = 0; i < fields.length; i++) {
    var $field = $(fields[i]);
    var nameKey = $($field).attr("column-reference");
    var id = $field.val();
    var fieldObj = {};
    fieldObj['id'] = id;
    fieldObj[nameKey] = $field.find('option:selected').attr('label');
    formObjForSign[$field.attr("name")] = fieldObj;
  }

  //  Чекбоксы
  fields = $("input.runaex[type=\"checkbox\"][data-schema]");
  for (i = 0; i < fields.length; i++) {
    formObjForSign[$(fields[i]).attr("name")] = $(fields[i]).prop('checked');
  }

  //  Радиобаттоны
  fields = $("input.runaex[type=\"radio\"]:checked[data-schema]");
  for (i = 0; i < fields.length; i++) {
    fieldObj = {};
    fieldObj["id"] = $(fields[i]).val();
    nameKey = $(fields[i]).attr("column-reference");
    fieldObj[nameKey] = $("input.runaex[type=\"radio\"]:checked[data-schema]+label")[i].textContent.trim();
    formObjForSign[$(fields[i]).attr("name")] = fieldObj;
  }

  //hidden input
  fields = $("input.runaex.save-hidden-input[type=\"hidden\"]:not([no_sign])");
  for (i = 0; i < fields.length; i++) {
    nameKey = $(fields[i]).attr("column-reference");
    if (nameKey != undefined) {
      fieldObj = {};
      fieldObj["id"] = $(fields[i]).val();
      visibleLabel = $(fields[i]).siblings('label');
      fieldObj[nameKey] = $(fields[i]).attr("str-value");
      formObjForSign[$(fields[i]).attr("name")] = fieldObj;
    }
    else if ($(fields[i]).attr("current-time-as-default-value") == "true") {
      formObjForSign[$(fields[i]).attr("name")] = extractedData.currentClientDate;
    }
    else if ($(fields[i]).attr("current-user-as-default-value") == "true") {
    }
    else if ($(fields[i]).attr("generate-automatically") == "true") {
    }
    else {
      formObjForSign[$(fields[i]).attr("name")] = $(fields[i]).val();
    }
  }

  //Autocomplete
  fields = $("input.runaex.ac-hidden-input[type=\"hidden\"][data-schema]");

  for (i = 0; i < fields.length; i++) {
    fieldObj = {};
    var visibleInput = $(fields[i]).siblings('input');
    fieldObj["id"] = $(fields[i]).val();
    if (visibleInput.hasClass('fias-input')) {
      fieldObj[visibleInput.attr("name")] = visibleInput.val();
      fieldObj["isHistorical"] = visibleInput.hasClass('fias-input-usageversion');
    }
    else {
      nameKey = $(fields[i]).attr("column-reference");
      fieldObj[nameKey] = visibleInput.val();
    }
    formObjForSign[$(fields[i]).attr("name")] = fieldObj;
  }

  //TextAreas
  fields = $("textarea.runaex");
  for (i = 0; i < fields.length; i++) {
    formObjForSign[$(fields[i]).attr("name")] = $(fields[i]).val();
  }

  // temporaly files are not sign
  // FileUpload fields
  fields = $("input.runaex[type=\"file\"][data-schema][sign-column-name]");
  var filled = false;
  $.each(fields, function (index, field) {
    if (field.files.length > 0)
      filled = true;
  });

  if (Object.getOwnPropertyNames(formObjForSign).length === 0 && !filled) {
    showAlert(NO_DATA_FOR_SIGN);
    return;
  }

  if (filled) {
    var filesEncoded = {};

    for (var j = 0; j < fields.length; j++) {
      filesEncoded[fields[j]] = "unsigned";
    }

    $.each(fields, function (index, field) {
      for (var i = 0; i < field.files.length; i++) {
        var file = field.files[i];
        var fileReader = new FileReader();
        fileReader['file'] = file;
        fileReader.onload = function (e) {
          var fileBase64 = e.target.result.split(",")[1];
          var signedFileData = signCreate(null, certSerialNumber, fileBase64);

          var fieldData = {
            "schema": $(field).attr("data-schema"),
            "table": $(field).attr("data-table"),
            "column": $(field).attr("sign-column-name"),
            "value": signedFileData,
            "tableId": $(field).attr("data-tableId")
          };
          data.push(fieldData);

          filesEncoded[field] = "signed";
        };
        fileReader.onerror = function (e) {
          filesEncoded[field] = "error";
        };
        fileReader.readAsDataURL(file);
      }
    });

    var interval = setInterval(function () {
      var unHandledExists = false;
      var errorExists = false;
      for (j = 0; j < fields.length; j++) {
        if (filesEncoded[fields[j]] == "unsigned") {
          unHandledExists = true;
          break;
        }
        if (filesEncoded[fields[j]] == "error") {
          errorExists = true;
          break;
        }
      }
      if (errorExists) {
        clearInterval(interval);
        showAlert(ERROR_DURING_FILE_SIGN);
      }
      else {
        if (!unHandledExists) {
          clearInterval(interval);
          signDataAndSend(data, formObjForSign, certSerialNumber, elem, extractedData);
        }
      }
    }, 250);
  }
  else {
    signDataAndSend(data, formObjForSign, certSerialNumber, elem, extractedData);
  }
}

function signAndSaveClickHandler(elem) {
  blockMainUI();
  setTimeout(function () {
    if (checkForPlugIn()) {
      elem.disabled = true;
      var extractedData = extractFormData();
      var valid = extractedData.valid;

      if (!valid) {
        elem.disabled = false;
        unblockMainUI();
        return;
      }

      var oStore = createObject("CAPICOM.Store");
      oStore.Open(CAPICOM_CURRENT_USER_STORE);
      var validCertArray = {};
      var count = 0;
      for (var i = 1; i <= oStore.Certificates.Count; i++) {
        var oCert = oStore.Certificates.Item(i);
        if (oCert.IsValid().Result == 1) {
          validCertArray[i] = oCert;
          count++;
        }
      }
      oStore.Close();
      if (count == 0) {
        showAlert(NO_VALID_CERTIFICATES);
      }
      else if (count == 1) {
        unblockMainUI();
        var certSerialNumber = validCertArray[1].SerialNumber;
        saveData(certSerialNumber, elem, extractedData);
      }
      else {
        showDialogToSelectCert(elem, extractedData, validCertArray);
      }
    }
    else {
      showAlert(NO_PLUGIN);
    }
  }, 250);
}

function showAlert(msg) {
  unblockMainUI();
  var $errorAlert = $('#errorAlert');
  var errorEl = $errorAlert.find(".alert");
  errorEl.empty();
  errorEl.append(msg);

  $errorAlert.modal("show");
}

function showDialogToSelectCert(elem, extractedData, validCertArray) {
  var $certDialog = $("#certDialog");
  var htmlDialog = "";

  htmlDialog += "<div id=\"certDialog\" class=\"modal hide fade in\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\" style=\"width:auto;\">";
  htmlDialog += "<div class=\"modal-header\">";
  htmlDialog += "<h3 id=\"certHeader\">" + CHOOSE_CERT + "</h3>";
  htmlDialog += "</div>";
  htmlDialog += "<div class=\"modal-body\">";
  htmlDialog += "<div id=\"certError\"></div>";
  htmlDialog += "<form id=\"certForm\" class=\"form-horizontal\" method=\"post\" accept-charset=\"UTF-8\">";
  htmlDialog += "<div class=\"control-group\">";
  htmlDialog += "<label class=\"control-label\" for=\"cert-sn\">" + CERT_SERIAL_NUM + "</label>";
  htmlDialog += "<div class=\"controls\">";
  htmlDialog += "<select id=\"cert-sn\" name=\"certSN\" class=\"combobox\" style=\"width:auto; max-width: 350px;\">";

  for (var i in validCertArray) {
    var oCert = validCertArray[i];
    var certSnStr = oCert.GetInfo(0).toString() + " (" + oCert.GetInfo(2) + VALID_TO;
    var validToDate = oCert.ValidToDate.toString();
    htmlDialog += "<option value=\"" + oCert.SerialNumber + "\">";
    htmlDialog += certSnStr + validToDate;
    htmlDialog += "</option>";
  }

  htmlDialog += "</select>";
  htmlDialog += "</div>";
  htmlDialog += "</div>";
  htmlDialog += "</form>";
  htmlDialog += "</div>";
  htmlDialog += "<div class=\"modal-footer\">";
  htmlDialog += "<input type=\"button\" id=\"selectCert\" value=\"Выбрать\" class=\"btn btn-primary\"/>";
  htmlDialog += "<input type=\"button\" id=\"cancelSelectCert\" value=\"Отмена\" class=\"btn\"data-dismiss=\"modal\" aria-hidden=\"true\"/>";
  htmlDialog += "</div>";
  htmlDialog += "</div>";

  if ($certDialog.length == 0) {
    $("body").append(htmlDialog);
  }
  else {
    $certDialog.replaceWith(htmlDialog);
  }

  setTimeout(function () {
    $('#selectCert').click(function () {
      $('#certDialog').modal("hide");
      saveData($('#cert-sn')[0].value, elem, extractedData);
    });

    $('#cancelSelectCert').click(function () {
      unblockMainUI();
      $('#certDialog').modal("hide");
    });

    var $certDialog2 = $("#certDialog");
    $certDialog2.on("show", function () {
      unblockMainUI();
    });

    $certDialog2.on("hide", function () {
      elem.disabled = false;
    });

    $certDialog2.modal("show");

  }, 250);
}