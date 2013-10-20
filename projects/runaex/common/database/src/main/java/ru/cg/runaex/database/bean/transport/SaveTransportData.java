package ru.cg.runaex.database.bean.transport;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 17.08.12
 * Time: 12:54
 *
 * @author Sabirov
 */
public class SaveTransportData implements Serializable {
  private static final long serialVersionUID = -5960598270320256745L;

  private Long processInstanceId;
  private Long id;
  private String schema;
  private String table;
  private List<Data> data;
  private List<Long> ids;

  public SaveTransportData(Long processInstanceId, Long id, String schema, String table, List<Data> data) {
    this.processInstanceId = processInstanceId;
    this.id = id;
    this.schema = schema;
    this.table = table;
    this.data = data;
  }

  public Long getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(Long processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public Long getId() {
    return id;
  }

  public String getSchema() {
    return schema;
  }

  public String getTable() {
    return table;
  }

  public List<Data> getData() {
    return data;
  }

  public Data getData(String field) {
    if (this.data != null) {
      for (Data data : this.getData()) {
        if (field != null && field.equals(data.getField())) {
          return data;
        }
      }
    }
    return null;
  }

  public void setDataType(String field, ClassType classType) {
    Data data = getData(field);
    if (data != null)
      data.setValueClass(classType.getSimpleName());
  }

  public List<Long> getIds() {
    return ids;
  }

  public void setIds(List<Long> ids) {
    this.ids = ids;
  }
}
