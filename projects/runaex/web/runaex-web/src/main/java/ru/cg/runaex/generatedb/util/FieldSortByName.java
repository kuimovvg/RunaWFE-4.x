package ru.cg.runaex.generatedb.util;

import java.util.Comparator;

import ru.cg.runaex.generatedb.bean.Field;

/**
 * Date: 14.08.12
 * Time: 14:13
 *
 * @author Sabirov
 */
public class FieldSortByName implements Comparator<Field> {
  public int compare(Field o1, Field o2) {
    return o1.getName().compareTo(o2.getName());
  }
}
