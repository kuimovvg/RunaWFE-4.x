package ru.cg.runaex.generatedb.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ru.cg.runaex.generatedb.GenerateDB;
import ru.cg.runaex.generatedb.bean.Field;

/**
 * Date: 14.08.12
 * Time: 10:18
 *
 * @author Sabirov
 */
public class FieldHashSet<F extends Field> extends HashSet<F> implements GenerateDB {
  private static final long serialVersionUID = -7379862131381503486L;

  @Override
  public String toString() {
    StringBuilder sbFields = new StringBuilder();
    List<F> sortList = getSortFieldList();
    for (Field field : sortList) {
      sbFields.append(field.toString()).append("\n");
    }
    return sbFields.toString();
  }

  @Override
  public String getSQL() {
    StringBuilder sb = new StringBuilder();
    List<F> sortList = getSortFieldList();
    for (F f : sortList) {
      if(sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(f.getSQL());
    }
    return sb.toString();
  }

  public List<F> getSortFieldList() {
    List<F> sortList = (List<F>) Arrays.asList(this.toArray(F.createEmptyObjects(size())));
    Collections.sort(sortList, new FieldSortByName());
    return sortList;
  }

  @Override
  public FieldHashSet clone() {
    return (FieldHashSet) super.clone();
  }

  /**
   * Check empty table
   *
   * @return true if table is empty
   */
  @Override
  public boolean isEmpty() {
    boolean empty = super.isEmpty();
    if(empty)
      return true;


    for (Field field : this) {
      if(field != null && !field.isEmpty()) {
        empty = false;
        break;
      }
    }
    return empty;
  }
}
