package ru.cg.runaex.components.bean.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.bean.component.part.SphinxIndexingColumn;
import ru.cg.runaex.components.bean.component.part.SphinxSaveIdColumn;
import ru.cg.runaex.components.bean.component.part.SphinxViewColumn;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.parser.SphinxSearchParser;
import ru.cg.runaex.components.util.RuTranslit;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author Абдулин Ильдар
 */
public class SphinxSearch extends EditableFieldImpl implements ComponentWithCustomValidation {
  private static final long serialVersionUID = -8433620776635894467L;

  private static final int TABLE = 0;
  private static final int INDEX_NAME = 1;
  private static final int USE_EXIST_INDEX = 2;
  private static final int INDEX_COLUMNS = 3;
  private static final int VIEW_COLUMNS = 4;
  private static final int COLUMNS_ID = 5;
  private static final int VISIBILITY_RULE = 6;
  private static final int EDITABILITY_RULE = 7;


  private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[a-zA-Z0-9\\-_]+");
  private static final Pattern BANNED_WORDS = Pattern.compile("(index)|(src)");

  private TableReference table;

  private List<SphinxViewColumn> viewColumns;
  private List<SphinxIndexingColumn> indexColumns;
  private List<SphinxSaveIdColumn> columnsId;

  @Override
  public int getParametersNumber() {
    return 8;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    table = parseTableReferenceInitTerm(getParameter(TABLE));

    String indexingColumns = getParameter(INDEX_COLUMNS);
    this.indexColumns = SphinxSearchParser.parseSphinxIndexingColumns(indexingColumns, getDefaultSchema());

    String viewColumns = getParameter(VIEW_COLUMNS);
    this.viewColumns = SphinxSearchParser.parseSphinxSearchColumn(viewColumns, getDefaultSchema());

    if (this.indexColumns != null && !this.indexColumns.isEmpty() && this.viewColumns != null && this.viewColumns.isEmpty()) {
      this.viewColumns = new ArrayList<SphinxViewColumn>();
      for (SphinxIndexingColumn indexingColumn : this.indexColumns) {
        SphinxViewColumn column = new SphinxViewColumn(indexingColumn.getReference().getColumn(), indexingColumn.getReference().clone());
        this.viewColumns.add(column);
      }
    }

    String columnsId = getParameter(COLUMNS_ID);
    this.columnsId = SphinxSearchParser.parseSphinxColumnsId(columnsId, getDefaultSchema());
  }

  @NotNullSchema
  @DatabaseStructureElement
  public String getSchema() {
    ensureFullyInitialized();
    return table.getSchema();
  }

  @NotNull
  @DatabaseStructureElement
  public String getTable() {
    ensureFullyInitialized();
    return table.getTable();
  }

  @NotNull
  public String getIndexName() {
    //RuTranslit используется для перевода русских букв в латиницу, это нужно поскольку Sphinx не понимает русские буквы
    return RuTranslit.translit(getParameter(INDEX_NAME));
  }

  @NotNull
  public Boolean isUseExistIndex() {
    return Boolean.valueOf(getParameter(USE_EXIST_INDEX));
  }

  @Valid
  public List<SphinxIndexingColumn> getIndexColumns() {
    ensureFullyInitialized();
    return indexColumns;
  }

  @Valid
  public List<SphinxViewColumn> getViewColumns() {
    ensureFullyInitialized();
    return viewColumns;
  }

  @Valid
  @NotNull  //todo replace by NotEmpty
  public List<SphinxSaveIdColumn> getColumnsId() {
    ensureFullyInitialized();
    return columnsId;
  }

  public String getIndexingColumnsStr() {
    ensureFullyInitialized();
    StringBuilder stringBuilder = new StringBuilder();
    for (SphinxIndexingColumn indexingColumn : this.indexColumns) {
      stringBuilder.append(indexingColumn).append(";");
    }
    return stringBuilder.substring(0, stringBuilder.length() - 1);
  }

  public String getViewColumnsStr() {
    ensureFullyInitialized();
    StringBuilder stringBuilder = new StringBuilder();
    for (SphinxViewColumn viewColumn : this.viewColumns) {
      stringBuilder.append(viewColumn).append(";");
    }
    return stringBuilder.substring(0, stringBuilder.length() - 1);
  }

  public void setIndexingColumns(String indexingColumns) {
    ensureFullyInitialized();
    this.indexColumns = SphinxSearchParser.parseSphinxIndexingColumns(indexingColumns, getDefaultSchema());
  }

  public void setViewColumns(String viewColumns) {
    ensureFullyInitialized();
    this.viewColumns = SphinxSearchParser.parseSphinxSearchColumn(viewColumns, getDefaultSchema());
  }

  @Override
  public GenerateFieldType getGenerateFieldType() {
    return GenerateFieldType.BIGINT;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  protected int getEditabilityRuleParameterIndex() {
    return EDITABILITY_RULE;
  }

  @Override
  public List<String> customValidate() {

    List<String> errors = new ArrayList<String>();
    if (!ALLOWED_CHARACTERS.matcher(getIndexName()).matches() || BANNED_WORDS.matcher(getIndexName()).matches()) {
      errors.add("SphinxSearch.indexName");
    }

    if (!isUseExistIndex()) {
      if (getIndexColumns().isEmpty())
        errors.add("SphinxSearch.indexColumnIsEmpty");
      if (getViewColumns().isEmpty())
        errors.add("SphinxSearch.viewColumnIsEmpty");
    }

    boolean found = false;
    if (getColumnsId() != null && !getViewColumns().isEmpty() && !getIndexColumns().isEmpty()) {
      for (SphinxSaveIdColumn sphinxSaveIdColumn : getColumnsId()) {
        if (sphinxSaveIdColumn.getReference().getTable() != null && sphinxSaveIdColumn.getReference().getSchema() != null) {
          for (SphinxViewColumn sphinxViewColumn : getViewColumns()) {
            if (sphinxViewColumn.getReference() != null && sphinxSaveIdColumn.getReference().getSchema().equals(sphinxViewColumn.getReference().getSchema()) && sphinxSaveIdColumn.getReference().getTable().equals(sphinxViewColumn.getReference().getTable())) {
              found = true;
              break;
            }
          }
          if (found)
            break;
          for (SphinxIndexingColumn sphinxIndexingColumn : getIndexColumns()) {
            if (sphinxIndexingColumn.getReference() != null && sphinxSaveIdColumn.getReference().getSchema().equals(sphinxIndexingColumn.getReference().getSchema()) && sphinxSaveIdColumn.getReference().getTable().equals(sphinxIndexingColumn.getReference().getTable())) {
              found = true;
              break;
            }
          }
          if (found)
            break;
        }
      }
    }

    if (!found && !isUseExistIndex()) {
      errors.add("SphinxSearch.columnsIdNotFounded");
    }
    return errors;
  }
}
