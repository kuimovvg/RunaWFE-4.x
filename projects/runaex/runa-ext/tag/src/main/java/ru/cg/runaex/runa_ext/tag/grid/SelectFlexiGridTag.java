package ru.cg.runaex.runa_ext.tag.grid;

import java.util.ArrayList;
import java.util.List;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.grid.SelectFlexiGrid;

/**
 * @author Sabirov
 */
public class SelectFlexiGridTag extends BaseGridTag<SelectFlexiGrid> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.SELECT_FLEXI_GRID;
  }

  @Override
  protected String getColumnsTableSchema(SelectFlexiGrid grid) {
    return grid.getSchema();
  }

  @Override
  protected String getColumnsTable(SelectFlexiGrid grid) {
    return grid.getTable();
  }

  @Override
  protected StringBuilder includeJsFiles(StringBuilder htmlBuilder) {
    appendComponentJsReference(FlexiGridTag, htmlBuilder);
    return htmlBuilder;
  }

  @Override
  protected List<Parameter> getFlexiGridTableAdditionalParameters(SelectFlexiGrid grid) {
    List<Parameter> parameters = new ArrayList<Parameter>(2);
    parameters.add(new Parameter("data-baseObject", grid.getMainTableReference().toString()));
    parameters.add(new Parameter("data-link", grid.getLinkTableReference().toString()));
    return parameters;
  }

  @Override
  protected List<Parameter> getFlexiGridConfigAdditionalParameters(SelectFlexiGrid grid) {
    List<Parameter> parameters = new ArrayList<Parameter>(2);
    parameters.add(new Parameter("baseObject", grid.getMainTableReference().toString()));
    parameters.add(new Parameter("link", grid.getLinkTableReference().toString()));
    return parameters;
  }
}
