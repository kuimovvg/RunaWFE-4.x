package ru.cg.runaex.database.bean;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;

/**
 * @author Абдулин Ильдар
 */
@Component
public class CSDependencyAdapter implements DependencyAdapter {

  @Autowired
  private BaseDao baseDao;

  @Override
  public List<Cell> getDependencies(Long processDefinitionId, Cell cell, FullTableParam param) {
    List<Cell> cells = new ArrayList<Cell>();
    for (Long id : getChildrenIds(processDefinitionId, cell, param)) {
      cells.add(new Cell(id, cell.getColumn()));
    }
    return cells;
  }

  @Override
  public String getBusinessRule(List<Cell> cells) {
    StringBuilder businessRule = new StringBuilder();
    for (Cell cell : cells) {
      if (businessRule.length() > 0) {
        businessRule.append("+");
      }
      businessRule.append(cell.getAlias());
    }
    return businessRule.toString();
  }

  protected List<Long> getChildrenIds(Long processDefinitionId, Cell cell, FullTableParam fullTableParam) {
    TransportData filter = new TransportData();
    filter.add(new Data(fullTableParam.getParentCol() + Data.EQ_POSTFIX, cell.getId(), "Long"));
    String column = fullTableParam.getParentCol();
    TransportDataSet transportDataSet = baseDao.getData(processDefinitionId, fullTableParam.getSchema(), fullTableParam.getTable(), new String[] {column}, null, null, null, null, null, null, filter, null, null, null);

    List<Long> childrenIds = new ArrayList<Long>();
    for (TransportData transportData : transportDataSet.getSets()) {
      childrenIds.add((Long) transportData.getData(fullTableParam.getTable()+"_id").getValue());
    }
    return childrenIds;
  }
}
