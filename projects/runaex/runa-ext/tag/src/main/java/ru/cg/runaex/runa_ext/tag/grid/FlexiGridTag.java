package ru.cg.runaex.runa_ext.tag.grid;

import java.util.Collections;
import java.util.List;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.grid.FlexiGrid;

/**
 * @author Sabirov
 */
public class FlexiGridTag extends BaseGridTag<FlexiGrid> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FLEXI_GRID;
  }

  @Override
  protected String getColumnsTableSchema(FlexiGrid grid) {
    return grid.getSchema();
  }

  @Override
  protected String getColumnsTable(FlexiGrid grid) {
    return grid.getTable();
  }

  @Override
  protected StringBuilder includeJsFiles(StringBuilder htmlBuilder) {
    appendComponentJsReference(FlexiGridTag, htmlBuilder);
    return htmlBuilder;
  }

  @Override
  protected List<Parameter> getFlexiGridTableAdditionalParameters(FlexiGrid grid) {
    return Collections.emptyList();
  }

  @Override
  protected List<Parameter> getFlexiGridConfigAdditionalParameters(FlexiGrid grid) {
    return Collections.emptyList();
  }
}
