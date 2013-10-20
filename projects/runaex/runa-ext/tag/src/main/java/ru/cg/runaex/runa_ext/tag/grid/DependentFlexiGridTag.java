package ru.cg.runaex.runa_ext.tag.grid;

import java.util.ArrayList;
import java.util.List;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.grid.DependentFlexiGrid;

/**
 * @author urmancheev
 */
public class DependentFlexiGridTag extends BaseGridTag<DependentFlexiGrid> {
  private static final long serialVersionUID = -6776885951369606971L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.DEPENDENT_FLEXI_GRID;
  }

  @Override
  protected String getColumnsTableSchema(DependentFlexiGrid grid) {
    return grid.getSchema();
  }

  @Override
  protected String getColumnsTable(DependentFlexiGrid grid) {
    return grid.getTable();
  }

  protected StringBuilder includeJsFiles(StringBuilder htmlBuilder) {
    appendComponentJsReference(DependentFlexiGridTag, htmlBuilder);
    return htmlBuilder;
  }

  protected List<Parameter> getFlexiGridTableAdditionalParameters(DependentFlexiGrid grid) {
    List<Parameter> parameters = new ArrayList<Parameter>(1);
    parameters.add(new Parameter("data-object1", grid.getMainTableReference().toString()));
    return parameters;
  }

  protected List<Parameter> getFlexiGridConfigAdditionalParameters(DependentFlexiGrid grid) {
    List<Parameter> parameters = new ArrayList<Parameter>(1);
    parameters.add(new Parameter("object1", grid.getMainTableReference().toString()));
    return parameters;
  }

  @Override
  protected boolean isDependent() {
    return true;
  }

  protected String getOnRowClickHandlerName() {
    return "onLinkTableSelectRowClickHandler";
  }
}
