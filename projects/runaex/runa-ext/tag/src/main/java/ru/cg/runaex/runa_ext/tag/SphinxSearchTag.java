package ru.cg.runaex.runa_ext.tag;

import java.util.*;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Input;

import com.cg.sphinx_tools.core.utils.Translit;
import ru.cg.fias.search.core.server.bean.SearchSphinxResult;
import ru.cg.fias.search.core.server.component.reader.SphinxBaseReader;
import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.SphinxSearch;
import ru.cg.runaex.components.bean.component.part.SphinxSaveIdColumn;
import ru.cg.runaex.components.bean.component.part.SphinxViewColumn;
import ru.cg.runaex.database.bean.model.SphinxSearchIndexingAndViewColumns;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.util.SphinxSearchConvertObject;

/**
 * @author Абдулин Ильдар
 */
public class SphinxSearchTag extends BaseEditableFreemarkerTag<SphinxSearch> {


  private static final String varMinSymbols = "3";
  private static final String varQueryDelay = "300";
  private static final long serialVersionUID = -1722587522211998340L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.SPHINX_SEARCH;
  }

  @Override
  protected String executeToHtml(SphinxSearch component) throws TemplateModelException {
    if (component.isUseExistIndex()) {
      SphinxSearchIndexingAndViewColumns indexingAndViewColumns = DatabaseSpringContext.getSphinxSearchService().getIndexingAndViewColumnsByIndexName(component.getIndexName());
      if (indexingAndViewColumns == null) {
        throw new RuntimeException("SphinxSearch: index " + component.getIndexName() + " does not exists");
      }
      component.setIndexingColumns(indexingAndViewColumns.getIndexingColumns());
      component.setViewColumns(indexingAndViewColumns.getViewColumns());
    }

    String schema = component.getSchema();
    String table = component.getTable();
    String fieldId = "sphinx_search_" + System.nanoTime();

    List<String> viewTables = new ArrayList<String>();

    Map<String, Object> labels = new HashMap<String, Object>();
    for (SphinxViewColumn columnParam : component.getViewColumns()) {
      StringBuilder builder = new StringBuilder();
      builder.append(columnParam.getReference().getSchema().charAt(0)).append(".").append(columnParam.getReference().getTable().charAt(0));
      if (!viewTables.contains(builder.toString())) {
        viewTables.add(builder.toString());
      }
      builder.append(".").append(columnParam.getReference().getColumn());
      String label = columnParam.getDisplayName();
      labels.put(builder.toString(), label);
    }

    String indexName = component.getIndexName();

    initObjectInfo(schema, table);

    Input input = new Input(Input.TEXT);
    input.setID(fieldId);
    input.setName(fieldId);
    input.setClass("runaex ac-input");

    Set<String> attrNames;
    Set<String> attrNamesDelta;

    Map<String, Integer> filters = new LinkedHashMap<String, Integer>();
    Map<String, Integer> filtersDelta = new LinkedHashMap<String, Integer>();
    try {
      attrNamesDelta = DatabaseSpringContext.createSphinxBaseReader(indexName + "_delta").getAttrNames();
      attrNames = DatabaseSpringContext.createSphinxBaseReader(indexName).getAttrNames();
    }
    catch (SphinxException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException("SphinxException: " + e.getMessage(), e);
    }

    Div hiddenElementsDiv = new Div();
    for (SphinxSaveIdColumn column : component.getColumnsId()) {
      String foreignTableIdColumn = column.getReference().getSchema().charAt(0) + "." + column.getReference().getTable().charAt(0) + "." + column.getReference().getTable() + "_id";
      Input inputHidden = new Input(Input.HIDDEN);
      inputHidden.setName(column.getColumnName());
      inputHidden.setID("sphinx_hidden_" + System.nanoTime());
      inputHidden.addAttribute("element-id", fieldId);
      inputHidden.addAttribute("data-schema", schema);
      inputHidden.addAttribute("data-table", table);
      inputHidden.setClass("runaex save-hidden-input sphinx-search-hidden");
      inputHidden.addAttribute("sphinx-search-id", foreignTableIdColumn);

      Long objectId = getValue(schema, table, column.getColumnName());

      if (objectId != null) {
        inputHidden.setValue(objectId);
        if (attrNames != null)
          for (String attr : attrNames) {
            if (Translit.reTranslit(attr).equals(foreignTableIdColumn.toUpperCase())) {
              filters.put(attr, objectId.intValue());
              break;
            }
          }
        if (attrNamesDelta != null)
          for (String attr : attrNamesDelta) {
            if (Translit.reTranslit(attr).equals(foreignTableIdColumn.toUpperCase())) {
              filtersDelta.put(attr, objectId.intValue());
              break;
            }
          }
      }

      if (!isEditable) {
        hiddenElementsDiv.addAttribute("disabled", "");
      }

      hiddenElementsDiv.addElement(inputHidden);
    }

    SphinxSearchConvertObject ssConvertObject = null;
    if (!filters.isEmpty() || !filtersDelta.isEmpty()) {
      try {
        List<SearchSphinxResult> result = null;

        if (!filtersDelta.isEmpty()) {
          SphinxBaseReader reader = DatabaseSpringContext.createSphinxBaseReader(indexName + "_delta");
          for (String attr : filtersDelta.keySet()) {
            reader.addFilter(attr, filtersDelta.get(attr));
          }
          result = reader.search("");
        }
        if ((result == null || result.isEmpty()) && !filters.isEmpty()) {
          SphinxBaseReader reader = DatabaseSpringContext.createSphinxBaseReader(indexName);
          for (String attr : filters.keySet()) {
            reader.addFilter(attr, filters.get(attr));
          }
          result = reader.search("");
        }
        if (result.size() == 1) {
          ssConvertObject = DatabaseSpringContext.getSphinxSearchUtil().convert(result.get(0), indexName);
        }
        else if (result.size() == 0) {
          logger.error("SphinxReader was returning zero count");
          throw new RuntimeException("SphinxReader was returning zero count");
        }
        else {
          logger.error("SphinxReader was returning more than one");
          throw new RuntimeException("SphinxReader was returning more than one");
        }
      }
      catch (SphinxException e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException("SphinxException: " + e.getMessage(), e);
      }
    }

    if (ssConvertObject != null) {
      input.setValue(ssConvertObject.getValue());
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
    }

    StringBuilder sb = new StringBuilder();
    String html = getHtmlWithValidation(Arrays.asList(new WrapComponent(null, input, hiddenElementsDiv)));
    sb.append(html);

    appendComponentCssReference(AutocompleteTag, sb);
    appendCssReference(JqueryUiAutoCompleteCss, sb);
    appendComponentJsReference(SphinxSearchTag, sb);


    Map<String, Object> ids = new HashMap<String, Object>();
    Map<String, Object> objects = new HashMap<String, Object>();
    if (ssConvertObject != null) {
      for (String key : ssConvertObject.getIdMap().keySet()) {
        ids.put(key, ssConvertObject.getIdMap().get(key));
      }
      for (String key : ssConvertObject.getObjectMap().keySet()) {
        objects.put(key, ssConvertObject.getObjectMap().get(key));
      }
    }

    setJsTemplateName(SphinxSearchTag);
    addObjectToJs("labels", labels);
    addObjectToJs("fieldId", fieldId);
    addObjectToJs("varMinSymbols", varMinSymbols);
    addObjectToJs("varQueryDelay", varQueryDelay);
    addObjectToJs("indexName", indexName);
    addObjectToJs("ids", ids);
    addObjectToJs("objects", objects);
    boolean editMode = false;
    if (!filters.isEmpty() || !filtersDelta.isEmpty()) {
      editMode = true;
    }
    addObjectToJs("editingMode", editMode);

    return sb.toString();
  }


  private Long getValue(String schema, String table, String field) throws TemplateModelException {
    Long selectedRowId = getSelectedRowId();
    if (selectedRowId != null) {
      return getValue(selectedRowId, schema, table, field, null, Long.class, null);
    }
    return null;
  }

}
