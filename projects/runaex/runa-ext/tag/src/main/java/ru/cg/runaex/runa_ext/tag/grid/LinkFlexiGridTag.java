package ru.cg.runaex.runa_ext.tag.grid;

import java.util.ArrayList;
import java.util.List;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.grid.LinkFlexiGrid;

/**
 * @author Sabirov
 */
public class LinkFlexiGridTag extends BaseGridTag<LinkFlexiGrid> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.LINK_FLEXI_GRID;
  }

  @Override
  protected String getColumnsTableSchema(LinkFlexiGrid grid) {
    return grid.getSecondaryTableReference().getSchema();
  }

  @Override
  protected String getColumnsTable(LinkFlexiGrid grid) {
    return grid.getSecondaryTableReference().getTable();
  }

  @Override
  protected StringBuilder includeJsFiles(StringBuilder htmlBuilder) {
    appendComponentJsReference(LinkFlexiGridTag, htmlBuilder);
    return htmlBuilder;
  }

  @Override
  protected List<Parameter> getFlexiGridTableAdditionalParameters(LinkFlexiGrid grid) {
    List<Parameter> parameters = new ArrayList<Parameter>(2);
    parameters.add(new Parameter("data-object1", grid.getMainTableReference().toString()));
    parameters.add(new Parameter("data-object2", grid.getSecondaryTableReference().toString()));
    return parameters;
  }

  @Override
  protected List<Parameter> getFlexiGridConfigAdditionalParameters(LinkFlexiGrid grid) {
    List<Parameter> parameters = new ArrayList<Parameter>(2);
    parameters.add(new Parameter("object1", grid.getMainTableReference().toString()));
    parameters.add(new Parameter("object2", grid.getSecondaryTableReference().toString()));
    return parameters;
  }

  @Override
  protected String getOnRowClickHandlerName() {
    return "onLinkTableSelectRowClickHandler";
  }
}
