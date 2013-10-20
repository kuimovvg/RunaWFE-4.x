package ru.cg.runaex.generatedb.bean;

import java.io.Serializable;
import java.util.List;

import ru.cg.runaex.generatedb.GenerateDBImpl;
import ru.cg.runaex.generatedb.util.TableHashSet;

/**
 * @author korablev
 */
public class Database extends GenerateDBImpl implements Serializable {
  private static final long serialVersionUID = -189753047899163794L;

  private TableHashSet<Table> tableHashSet;
  private List<Sequence> sequenceList;

  public TableHashSet<Table> getTables() {
    return tableHashSet;
  }

  public void setTables(TableHashSet<Table> tableHashSet) {
    this.tableHashSet = tableHashSet;
  }

  public List<Sequence> getSequenceList() {
    return sequenceList;
  }

  public void setSequenceList(List<Sequence> sequenceList) {
    this.sequenceList = sequenceList;
  }

  @Override
  public String getSQL() {
    return getTables().getSQL() + getSequenceListSql();
  }

  public String getSequenceListSql() {
    StringBuilder sb = new StringBuilder();
    for (Sequence sequence : getSequenceList()) {
        sb.append(sequence.getSQL());
    }
    return sb.toString();
  }
}
