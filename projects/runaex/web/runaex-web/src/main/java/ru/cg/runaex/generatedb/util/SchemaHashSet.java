package ru.cg.runaex.generatedb.util;

import java.util.HashSet;

import ru.cg.runaex.generatedb.GenerateDB;
import ru.cg.runaex.generatedb.bean.Schema;

/**
 * Date: 14.08.12
 * Time: 10:18
 *
 * @author Sabirov
 */
public class SchemaHashSet<S extends Schema> extends HashSet<S> implements GenerateDB {
  @Override
  public String getSQL() {
   StringBuilder sb = new StringBuilder();
    for(S s : this) {
      sb.append(s.getSQL());
    }
    return sb.toString();
  }
}
