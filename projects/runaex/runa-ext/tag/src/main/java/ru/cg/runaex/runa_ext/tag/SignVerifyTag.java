package ru.cg.runaex.runa_ext.tag;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.jul.sign.cryptopro.CryptoProSignUtils;
import com.jul.sign.cryptopro.exception.SignException;
import com.jul.sign.cryptopro.exception.SignVerifyException;
import freemarker.template.TemplateModelException;
import org.apache.commons.codec.binary.Base64;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.SignVerify;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.runa_ext.tag.field.BaseFieldFreemarkerTag;
import ru.cg.runaex.runa_ext.tag.utils.FiasAddressUtils;


/**
 * @author Абдулин Ильдар
 */
public class SignVerifyTag extends BaseFieldFreemarkerTag<SignVerify> {

  private static class SignVerifyInDbException extends SignVerifyException {
    private static final long serialVersionUID = 2227525540373224612L;

    private SignVerifyInDbException() {
    }

    private SignVerifyInDbException(String message) {
      super(message);
    }

    private SignVerifyInDbException(String message, Throwable cause) {
      super(message, cause);
    }

    private SignVerifyInDbException(Throwable cause) {
      super(cause);
    }
  }

  private static final long serialVersionUID = 7096196967989459418L;
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
  private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  private static final Base64 base64 = new Base64();

  private static final String IS_HISTORICAL_KEY = "isHistorical";
  private static final String ID_KEY = "id";

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.SIGN_VERIFY;
  }

  @Override
  protected String executeToHtml(SignVerify component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    String schema = component.getSchema();
    String table = component.getTable();

    StringBuilder html = new StringBuilder();
    initObjectInfo(schema, table);

    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null) {
      String sign = getValue(selectedRowId, schema, component.getTable(), component.getSignColumn(), null, String.class, null);
      String data = getValue(selectedRowId, schema, component.getTable(), component.getDataColumn(), null, String.class, null);
      if (sign != null && data != null) {
        StringBuilder content = new StringBuilder();
        String fieldId = "sign_verify_" + System.nanoTime();
        boolean verify = false;

        try {
          CryptoProSignUtils.verify(sign.getBytes("utf-8"), data);
          if (!"{}".equals(data)) {
            verifyData(schema, table, data);
            html.append("<div style='color:green'><img src='resources/images/signature-ok.png'/><div style='display:inline; margin-left:10px'>")
                .append(resourceBundleMessageSource.getMessage("signVerified", null, Locale.ROOT))
                .append("</div></div>");
          }
          else {
            html.append("<div>").append(resourceBundleMessageSource.getMessage("dataNotFound", null, Locale.ROOT)).append("</div>");
          }
          verify = true;
        }
        catch (SignVerifyInDbException ex) {
          content.append(ex.getMessage());
          html.append("<div style='color:red; cursor:pointer;'><img src='resources/images/signature-bad.png'/><div id='")
              .append(fieldId)
              .append("' style='display:inline; margin-left:10px'>").append(resourceBundleMessageSource.getMessage("signVerifyFailed", null, Locale.ROOT)).append("</div></div>");
        }
        catch (SignVerifyException ex) {
          content.append(resourceBundleMessageSource.getMessage("signInTheDocNotVerified", null, Locale.ROOT));
          html.append("<div style='color:red; cursor:pointer;'><img src='resources/images/signature-bad.png'/><div id='").append(fieldId).append("' style='display:inline; margin-left:10px'>Подпись неверна</div></div>");
        }
        catch (SignException e) {
          html.append("<div style='color:red'><img src='resources/images/signature-bad.png'/><div style='display:inline; margin-left:10px'>")
              .append(resourceBundleMessageSource.getMessage("verifyError", null, Locale.ROOT))
              .append("</div></div>");
        }
        catch (UnsupportedEncodingException e) {
          html.append("<div style='color:red'><img src='resources/images/signature-bad.png'/><div style='display:inline; margin-left:10px'>").append(resourceBundleMessageSource.getMessage("verifyError", null, Locale.ROOT)).append("</div></div>");
          logger.error(e.getMessage(), e);
        }
        if (!verify) {
          setJsTemplateName(SignVerifyTag);
          addObjectToJs("fieldId", fieldId);
          addObjectToJs("verifyError", resourceBundleMessageSource.getMessage("verifyError", null, Locale.ROOT));
          addObjectToJs("content", content.toString());
        }
      }
      else
        html.append("<div>").append(resourceBundleMessageSource.getMessage("signNotFound", null, Locale.ROOT)).append("</div>");
    }
    return html.toString();
  }

  protected void verifyData(String schema, String table, String data) throws SignException, TemplateModelException, AuthorizationException, TaskDoesNotExistException, AuthenticationException {
    boolean allColumnsFounded = true;
    StringBuilder content = new StringBuilder();
    Long selectedRowId = getSelectedRowId();
    List<String> columnsInDb = DatabaseSpringContext.getBaseDao().getColumns(getProcessDefinitionId(), schema, table);
    Map<String, Object> dataMap = GSON.fromJson(data, Map.class);
    for (String key : dataMap.keySet()) {
      if (key != null && !columnsInDb.contains(key)) {
        allColumnsFounded = false;
        break;
      }
    }
    if (!allColumnsFounded) {
      throw new SignVerifyInDbException(resourceBundleMessageSource.getMessage("databaseStructureChanged", null, Locale.ROOT));
    }
    else {
      TransportData transportData = DatabaseSpringContext.getBaseDao().getDataById(getProcessDefinitionId(), schema, table, dataMap.keySet(), selectedRowId);
      for (String column : dataMap.keySet()) {
        //Если вложенные объекты(это для combobox,autocomplete,fiasaddress)
        if (dataMap.get(column) instanceof Map) {
          Map<String, Object> refMap = (Map) dataMap.get(column);
          //Относится к адресу?
          if (refMap.containsKey(IS_HISTORICAL_KEY)) {
            content.append(checkFias(refMap, transportData, schema, table, column, selectedRowId));
          }
          else {
            content.append(checkComboBox(refMap, transportData, column));
          }
        }
        else {
          Object dbValue = transportData.getData(column).getValue();
          String jsonValue = dataMap.get(column) != null ? dataMap.get(column).toString() : "";
          String dbStringValue = objectToString(dbValue, jsonValue);

          if (!dbStringValue.equals(jsonValue)) {
            content.append(resourceBundleMessageSource.getMessage("changeField", new Object[] {column}, Locale.ROOT));
          }
        }
      }
      if (content.length() != 0) {
        throw new SignVerifyInDbException(resourceBundleMessageSource.getMessage("dbValuesChanged", null, Locale.ROOT) + content);
      }
    }
  }

  private StringBuilder checkFias(Map<String, Object> refMap, TransportData dbData, String schema, String table, String column, Long selectedRowId) throws SignException, TemplateModelException {
    StringBuilder content = new StringBuilder();
    if (refMap.get(IS_HISTORICAL_KEY) instanceof String && ((String) refMap.get(IS_HISTORICAL_KEY)).isEmpty()) {
      throw new SignException(IS_HISTORICAL_KEY + " can not be empty");
    }
    String id = "";
    String address = "";
    String addressColumn = "";
    for (String key : refMap.keySet()) {
      if (refMap.get(key) instanceof String) {
        if (ID_KEY.equals(key)) {
          id = (String) refMap.get(key);
        }
        else if (!IS_HISTORICAL_KEY.equals(key)) {
          address = (String) refMap.get(key);
          addressColumn = key;
        }
      }
    }

    if (Boolean.FALSE.equals(refMap.get(IS_HISTORICAL_KEY))) {
      String guIdInDb = (String) dbData.getData(column).getValue();
      String fullAddressInDb = FiasAddressUtils.getAddressByGuId(guIdInDb);
      if (!guIdInDb.equals(id) || !fullAddressInDb.equals(address)) {
        content.append(resourceBundleMessageSource.getMessage("changeField", new Object[] {column}, Locale.ROOT));
      }
    }
    else {
      String guIdInDb = (String) dbData.getData(column).getValue();
      if (!guIdInDb.equals(id)) {
        content.append(resourceBundleMessageSource.getMessage("changeField", new Object[] {column}, Locale.ROOT));
      }
      String addressInDb = getValue(selectedRowId, schema, table, addressColumn, null, String.class, null);
      if (addressInDb == null)
        addressInDb = "";
      if (!address.equals(addressInDb)) {
        content.append(resourceBundleMessageSource.getMessage("changeField", new Object[] {addressColumn}, Locale.ROOT));
      }
    }
    return content;
  }

  private StringBuilder checkComboBox(Map<String, Object> refMap, TransportData dbData, String column) throws SignException, TemplateModelException, AuthorizationException, TaskDoesNotExistException, AuthenticationException {
    StringBuilder content = new StringBuilder();
    Long id = null;
    String ref = null;
    String refValue = null;
    for (String refKey : refMap.keySet()) {
      if (ID_KEY.equals(refKey)) {
        if (!((String) refMap.get(ID_KEY)).isEmpty())
          id = Long.valueOf(((String) refMap.get(ID_KEY)));
        else
          break;
      }
      else {
        refValue = (String) refMap.get(refKey);
        ref = refKey;
      }
    }
    if (id != null && ref != null) {
      ColumnReference reference = ComponentParser.parseColumnReference(ref, null);
      Long refIdInDb = (Long) dbData.getData(column).getValue();
      if (refIdInDb != null) {
        String refValueInDb = getValue(refIdInDb, reference.getSchema(), reference.getTable(), reference.getColumn(), null, String.class, null);
        if (!refIdInDb.equals(id) || !refValueInDb.equals(refValue)) {
          content.append(resourceBundleMessageSource.getMessage("changeField", new Object[] {column}, Locale.ROOT));
        }
      }
    }
    return content;
  }

  /**
   * Преобразование поля простого типа в String
   *
   * @param dbValue   - значение для преобразования
   * @param jsonValue - значение как хранится в json
   * @return
   * @throws SignException
   */
  public String objectToString(Object dbValue, String jsonValue) throws SignException {
    String dbStringValue = "";
    if (dbValue instanceof Date) {
      try {
        dateTimeFormat.parse(jsonValue);
        dbStringValue = dateTimeFormat.format(dbValue);
      }
      catch (ParseException e) {
        try {
          dateFormat.parse(jsonValue);
          dbStringValue = dateFormat.format(dbValue);
        }
        catch (ParseException e1) {
          throw new SignException("date format must be " + dateFormat.toPattern() + " or " + dateTimeFormat.toPattern());
        }
      }
    }
    else if (dbValue instanceof byte[]) {
      dbStringValue = new String(base64.encode((byte[]) dbValue));
    }
    else {
      if (dbValue != null)
        dbStringValue = dbValue.toString();
    }
    return dbStringValue;
  }

}
