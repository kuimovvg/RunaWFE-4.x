package ru.cg.runaex.database.bean.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 17.08.12
 * Time: 12:54
 *
 * @author Sabirov
 */
public class TransportData implements Serializable {
  private static final long serialVersionUID = 6662614332004977610L;
  private Integer num;
  private List<Data> data;

  public TransportData() {
  }

  public TransportData(Integer num, List<Data> row) {
    this.num = num;
    this.data = row;
  }

  public Integer getNum() {
    return num;
  }

  public void setNum(Integer num) {
    this.num = num;
  }

  public List<Data> getData() {
    if (this.data == null) {
      this.data = new ArrayList<Data>();
    }
    return data;
  }

  public void setData(List<Data> data) {
    this.data = data;
  }

  public void add(Data item) {
    if (this.data == null) {
      this.data = new ArrayList<Data>();
    }
    this.data.add(item);
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

  /**
   * @param field     - field (kirillica or latinica)
   * @param classType - class type
   */
  public void setDataType(String field, ClassType classType, boolean useTranslit) {
    if (this.data != null) {
      for (Data data : this.getData()) {
        if (field == null) {
          continue;
        }
        String dataField = data.getFieldWithoutComparison();
        if (field.equals(dataField)) {
          data.setValueClass(classType.getSimpleName());
        }
      }
    }
  }
}
