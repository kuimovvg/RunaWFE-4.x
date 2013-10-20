package ru.cg.runaex.components.bean.component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.util.FileUploadComponentHelper;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author urmancheev
 */
public abstract class BaseFile extends Component implements ComponentWithSingleField {
  private static final long serialVersionUID = -995822685023674189L;

  private ColumnReference field;
  private String signColumnName;
  private ColumnReference defaultValueReferenceName;
  private ColumnReference defaultValueReferenceFile;

  @Override
  protected void initLazyFields() {
    super.initLazyFields();
    field = parseColumnReferenceInitTerm(getParameter(getFieldParameterIndex()));
    ColumnReference signColumnReference = parseColumnReference(getParameter(getSignColumnNameParameterIndex()));
    signColumnName = signColumnReference != null ? signColumnReference.getColumn() : null;

    defaultValueReferenceName = parseColumnReference(getDefaultValueStr());
    if (defaultValueReferenceName != null) {
      defaultValueReferenceFile = FileUploadComponentHelper.getFileColumnReference(defaultValueReferenceName);
    }
  }

  @NotNullSchema
  @DatabaseStructureElement
  public String getSchema() {
    ensureFullyInitialized();
    return field.getSchema();
  }

  @NotNull
  @DatabaseStructureElement
  public String getTable() {
    ensureFullyInitialized();
    return field.getTable();
  }

  @NotNull
  @DatabaseStructureElement
  public String getField() {
    ensureFullyInitialized();
    return field.getColumn();
  }

  public boolean isSignRequired() {
    return getParameter(getSignColumnNameParameterIndex()) != null;
  }

  @DatabaseStructureElement
  public String getSignColumnName() {
    return signColumnName;
  }

  public Boolean isDefaultValueSet() {
    ensureFullyInitialized();
    return defaultValueReferenceName != null;
  }

  public String getDefaultValueStr() {
    return getParameter(getDefaultValueParameterIndex());
  }

  @Valid
  public ColumnReference getDefaultValueReferenceName() {
    ensureFullyInitialized();
    return defaultValueReferenceName;
  }

  public ColumnReference getDefaultValueReferenceFile() {
    ensureFullyInitialized();
    return defaultValueReferenceFile;
  }

  protected abstract int getFieldParameterIndex();

  protected abstract int getSignColumnNameParameterIndex();

  protected abstract int getDefaultValueParameterIndex();
}
