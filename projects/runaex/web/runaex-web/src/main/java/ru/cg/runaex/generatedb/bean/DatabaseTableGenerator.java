package ru.cg.runaex.generatedb.bean;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.bean.component.*;
import ru.cg.runaex.components.bean.component.field.*;
import ru.cg.runaex.components.bean.component.grid.DependentFlexiGrid;
import ru.cg.runaex.components.bean.component.grid.FlexiGrid;
import ru.cg.runaex.components.bean.component.grid.LinkFlexiGrid;
import ru.cg.runaex.components.bean.component.grid.TreeGrid;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DefaultValueType;
import ru.cg.runaex.components.bean.component.part.SphinxSaveIdColumn;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.components.util.FileUploadComponentHelper;
import ru.cg.runaex.database.bean.FtlComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sabirov
 */
public class DatabaseTableGenerator {

  public static List<Table> createTable(FtlComponent ftlComponent) throws RuntimeException {
    switch (ftlComponent.getComponentType()) {
      case LINK_FLEXI_GRID:
        return DatabaseTableGenerator.createLinkTable(ftlComponent);
      case DEPENDENT_FLEXI_GRID:
        return wrapToList(DatabaseTableGenerator.createDependentTable(ftlComponent));
      case AUTOCOMPLETE:
        List<Table> tables = wrapToList(DatabaseTableGenerator.createAutocomplete(ftlComponent));
        Autocomplete autocomplete = ftlComponent.getComponent();
        if (autocomplete.getRelatedLinkTable() != null) {
          tables.add(DatabaseTableGenerator.createAutocompleteLinkTable(ftlComponent));
        }
        if (autocomplete.getDefaultValue() != null && autocomplete.getDefaultValue().getType() == DefaultValueType.FROM_DB) {
          ColumnReference defaultValueReference = ComponentParser.parseColumnReference(autocomplete.getDefaultValue().getValue(), null);
          tables.add(createTable(defaultValueReference, autocomplete.getColumnReference(), GenerateFieldType.BIGINT));
        }
        return tables;
      case CHECK_BOX:
        tables = wrapToList(DatabaseTableGenerator.createCheckBox(ftlComponent));
        CheckBox checkBox = ftlComponent.getComponent();
        if (checkBox.getCopyFromReference() != null && checkBox.isUseDefaultValueFromDb()) {
          tables.add(createTable(checkBox.getCopyFromReference(), null, GenerateFieldType.BOOLEAN));
        }
        return tables;
      case COMBO_BOX:
        tables = wrapToList(DatabaseTableGenerator.createComboBox(ftlComponent));
        ComboBox comboBox = ftlComponent.getComponent();
        if (comboBox.getDefaultValue() != null && comboBox.getDefaultValue().getType() == DefaultValueType.FROM_DB) {
          ColumnReference defaultValueReference = ComponentParser.parseColumnReference(comboBox.getDefaultValue().getValue(), null);
          tables.add(createTable(defaultValueReference, comboBox.getColumnReference(), GenerateFieldType.BIGINT));
        }
        return tables;
      case DATE_TIME_PICKER:
        tables = wrapToList(DatabaseTableGenerator.createDateTimePicker(ftlComponent));
        DateTimePicker dateTimePicker = ftlComponent.getComponent();
        if (dateTimePicker.getDefaultValue() != null && dateTimePicker.getDefaultValue().getType() == DefaultValueType.FROM_DB) {
          ColumnReference defaultValueReference = ComponentParser.parseColumnReference(dateTimePicker.getDefaultValue().getValue(), null);
          tables.add(createTable(defaultValueReference, null, dateTimePicker.getGenerateFieldType()));
        }
        return tables;
      case RADIO_BUTTON_GROUP:
        tables = wrapToList(DatabaseTableGenerator.createRadioButtonGroup(ftlComponent));
        RadioButtonGroup radioButtonGroup = ftlComponent.getComponent();
        if (radioButtonGroup.getDefaultValue() != null && radioButtonGroup.getDefaultValue().getType() == DefaultValueType.FROM_DB) {
          ColumnReference defaultValueReference = ComponentParser.parseColumnReference(radioButtonGroup.getDefaultValue().getValue(), null);
          tables.add(createTable(defaultValueReference, radioButtonGroup.getColumnReference(), GenerateFieldType.BIGINT));
        }
        return tables;
      case TEXT_FIELD:
        return wrapToList(DatabaseTableGenerator.createTextField(ftlComponent));
      case FLEXI_GRID:
        return wrapToList(DatabaseTableGenerator.createFlexiTable(ftlComponent));
      case TREE_GRID:
      case SELECT_TREE_GRID:
        return wrapToList(DatabaseTableGenerator.createTreeGrid(ftlComponent));
      case FILE_UPLOAD:
      case FILE_VIEW:
        return wrapToList(DatabaseTableGenerator.createFileField(ftlComponent));
      case HIDDEN_INPUT:
        return wrapToList(DatabaseTableGenerator.createHiddenInputField(ftlComponent));
      case FIAS_ADDRESS:
        return wrapToList(DatabaseTableGenerator.createAddressField(ftlComponent));
      case SPHINX_SEARCH:
        return wrapToList(DatabaseTableGenerator.createSphinxSearch(ftlComponent));
      case SIGN_AND_SAVE_BUTTON:
        return wrapToList(DatabaseTableGenerator.createSignAndSaveButton(ftlComponent));
      case RECORD_NUMBER_GENERATOR:
        return wrapToList(DatabaseTableGenerator.createRecordNumberGeneratorField(ftlComponent));
      case NUMBER_FIELD:
        return wrapToList(DatabaseTableGenerator.createNumberField(ftlComponent));
    }
    return null;
  }

  private static Table createRecordNumberGeneratorField(FtlComponent ftlComponent) {
    RecordNumberGenerator component = ftlComponent.getComponent();
    Table table = new Table();
    Field field = new Field(component.getGenerateFieldType());

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    field.initField(table, component.getField(), component);
    table.setField(field);
    return table;
  }

  private static Table createSignAndSaveButton(FtlComponent ftlComponent) {
    SignAndSaveButton signAndSaveButton = ftlComponent.getComponent();
    Table table = new Table();
    Field dataForSign = new Field(GenerateFieldType.TEXT);
    Field signDataField = new Field(GenerateFieldType.TEXT);

    String schemaName = signAndSaveButton.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(signAndSaveButton.getTable());

    dataForSign.initField(table, signAndSaveButton.getDataField(), signAndSaveButton);
    signDataField.initField(table, signAndSaveButton.getSignField(), signAndSaveButton);

    table.setField(dataForSign);
    table.setField(signDataField);
    return table;
  }

  private static ArrayList<Table> wrapToList(Table table) {
    ArrayList<Table> list = new ArrayList<Table>();
    list.add(table);
    return list;
  }

  public static Table createFlexiTable(FtlComponent ftlComponent) {
    FlexiGrid flexiGrid = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(flexiGrid.getGenerateFieldType());

    String schemaName = flexiGrid.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(flexiGrid.getTable());
    field.initFieldByConfigTable(table, flexiGrid.getColumnsStr(), flexiGrid);

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static List<Table> createLinkTable(FtlComponent ftlComponent) {
    LinkFlexiGrid linkFlexiGrid = ftlComponent.getComponent();

    List<Table> tables = new ArrayList<Table>(2);

    Table linkTable = new Table();
    Field field1 = new Field();
    Field field2 = new Field();

    String schemaName = linkFlexiGrid.getSchema();
    linkTable.setSchema(new Schema(schemaName));
    linkTable.setName(linkFlexiGrid.getTable());

    String mainTableSchemaName = linkFlexiGrid.getMainTableReference().getSchema();
    String mainTableName = linkFlexiGrid.getMainTableReference().getTable();

    Table mainTable = new Table();
    mainTable.setSchema(new Schema(mainTableSchemaName));
    mainTable.setName(mainTableName);
    String pkField = mainTableName + Table.POSTFIX_TABLE_ID;
    Field refField = new Field();
    refField.setName(pkField);
    refField.setType(GenerateFieldType.BIGINT);
    refField.setLength(null);
    mainTable.setField(refField);
    field1.setName(pkField);
    field1.setComment(linkFlexiGrid.getMainTableReferenceStr());
    field1.setType(GenerateFieldType.BIGINT);
    field1.setLength(null);
    field1.setReferences(new References(linkTable, mainTable, field1));

    String secondaryTableSchemaName = linkFlexiGrid.getSecondaryTableReference().getSchema();
    String secondaryTableName = linkFlexiGrid.getSecondaryTableReference().getTable();

    Table secondaryTable = new Table();
    secondaryTable.setSchema(new Schema(secondaryTableSchemaName));
    secondaryTable.setName(secondaryTableName);
    pkField = secondaryTableName + Table.POSTFIX_TABLE_ID;
    refField = new Field();
    refField.setName(pkField);
    refField.setType(GenerateFieldType.BIGINT);
    refField.setLength(null);
    secondaryTable.setField(refField);
    field2.setName(pkField);
    field2.setComment(linkFlexiGrid.getSecondaryTableReferenceStr());
    field2.setType(GenerateFieldType.BIGINT);
    field2.setLength(null);
    field2.setReferences(new References(linkTable, secondaryTable, field2));

    Table refTable = field2.getReferences().getRefTable();

    Component secondaryTableComponent = ComponentUtil.createComponent(ComponentType.FLEXI_GRID);
    String[] secondaryTableComponentParameters = {refTable.getSchema().getName(), refTable.getName(), linkFlexiGrid.getColumnsStr()};
    secondaryTableComponent.init("FlexiGrid", ComponentType.FLEXI_GRID, secondaryTableComponentParameters);
    secondaryTableComponent.setDefaultSchema(ftlComponent.getDefaultSchema());

    FtlComponent obj2Component = new FtlComponent(ftlComponent.getProcessName(), secondaryTableComponent, null);
    ftlComponent.addForms(ftlComponent.getSourceForms());
    Table object2Table = createFlexiTable(obj2Component);
    tables.add(object2Table);

    if (!field1.isEmpty() && !field2.isEmpty()) {
      linkTable.setField(field1);
      linkTable.setField(field2);
    }
    tables.add(linkTable);
    return tables;
  }

  public static Table createDependentTable(FtlComponent ftlComponent) {
    Table table;
    Table refTable = new Table();
    Field reference = new Field();

    DependentFlexiGrid grid = ftlComponent.getComponent();

    String refSchemaName = grid.getMainTableReference().getSchema();
    String refTableName = grid.getMainTableReference().getTable();

    refTable.setSchema(new Schema(refSchemaName));
    String pkField = refTableName + Table.POSTFIX_TABLE_ID;
    refTable.setName(refTableName);
    Field refField = new Field();
    refField.setName(pkField);
    refField.setType(GenerateFieldType.BIGINT);
    refField.setLength(null);
    refTable.setField(refField);
    reference.setName(pkField);
    reference.setComment(grid.getMainTableReferenceStr());
    reference.setType(GenerateFieldType.BIGINT);
    reference.setLength(null);

    Component dependentTableComponent = ComponentUtil.createComponent(ComponentType.FLEXI_GRID);
    String[] dependentTableParameters = {grid.getSchema(), grid.getTable(), grid.getColumnsStr()};
    dependentTableComponent.init("FlexiGrid", ComponentType.FLEXI_GRID, dependentTableParameters);
    dependentTableComponent.setDefaultSchema(ftlComponent.getDefaultSchema());

    FtlComponent dependentTableFtlComponent = new FtlComponent(ftlComponent.getProcessName(), dependentTableComponent, null);
    ftlComponent.addForms(ftlComponent.getSourceForms());
    table = createFlexiTable(dependentTableFtlComponent);

    reference.setReferences(new References(table, refTable, reference));
    table.setField(reference);

    return table;
  }

  public static Table createAutocomplete(FtlComponent ftlComponent) {
    Autocomplete autocomplete = ftlComponent.getComponent();
    Table table = new Table();
    Field field = new Field(autocomplete.getGenerateFieldType());
    Field relatedTableReferenceField = new Field();
    References ref;

    String schemaName = autocomplete.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(autocomplete.getTable());
    field.initField(table, autocomplete.getField(), autocomplete);

    field.createReferences(table, autocomplete.getColumnReference());
    ref = field.getReferences();

    field.setObligatory(autocomplete.getRequireRule().isUnconditionallyRequired());

    if (ref != null && autocomplete.getRelatedTableReference() != null && autocomplete.isDefaultValueNotSpecified()) {
      String relatedTableSchema = autocomplete.getRelatedTableReference().getSchema();
      String relatedTableName = autocomplete.getRelatedTableReference().getTable();

      relatedTableReferenceField.setObligatory(true);
      relatedTableReferenceField.setType(GenerateFieldType.BIGINT);
      relatedTableReferenceField.setName(relatedTableName.concat(Table.POSTFIX_TABLE_ID));
      Table relatedTable = new Table();
      relatedTable.setSchema(new Schema(relatedTableSchema));
      relatedTable.setName(relatedTableName);
      relatedTableReferenceField.setReferences(new References(ref.getRefTable(), relatedTable, relatedTableReferenceField));
      ref.getRefTable().setField(relatedTableReferenceField);
    }
    if (ref != null && autocomplete.getRelatedTableColumn() != null && autocomplete.isDefaultValueNotSpecified()) {
      relatedTableReferenceField.setName(autocomplete.getRelatedTableColumn());
    }

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createCheckBox(FtlComponent ftlComponent) {
    CheckBox checkBox = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(checkBox.getGenerateFieldType());

    String schemaName = checkBox.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(checkBox.getTable());
    field.initField(table, checkBox.getField(), checkBox);

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createComboBox(FtlComponent ftlComponent) {
    ComboBox comboBox = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(comboBox.getGenerateFieldType());

    String schemaName = comboBox.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(comboBox.getTable());
    field.initField(table, comboBox.getField(), comboBox);
    field.createReferences(table, comboBox.getColumnReference());
    field.setObligatory(comboBox.getRequireRule().isUnconditionallyRequired());

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createDateTimePicker(FtlComponent ftlComponent) {
    DateTimePicker component = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(component.getGenerateFieldType());

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    field.initField(table, component.getField(), component);
    field.setObligatory(component.getRequireRule().isUnconditionallyRequired());

    field.setType(component.getGenerateFieldType());

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createRadioButtonGroup(FtlComponent ftlComponent) {
    RadioButtonGroup component = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(component.getGenerateFieldType());

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    field.initField(table, component.getField(), component);
    field.createReferences(table, component.getColumnReference());
    field.setObligatory(component.getRequireRule().isUnconditionallyRequired());

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createTextField(FtlComponent ftlComponent) {
    TextField component = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(GenerateFieldType.VARCHAR);

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    field.initField(table, component.getField(), component);
    field.setRegex(component.getRegex());
    field.setLength(component.getLength() > 0 ? component.getLength() : Table.DEFAULTS_FIELD_LENGTH);
    field.setObligatory(component.getRequireRule().isUnconditionallyRequired());

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  private static Table createNumberField(FtlComponent ftlComponent) {
    NumberField component = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(GenerateFieldType.NUMERIC);

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    field.initField(table, component.getField(), component);
    field.setObligatory(component.getRequireRule().isUnconditionallyRequired());
    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createTreeGrid(FtlComponent ftlComponent) {
    TreeGrid component = ftlComponent.getComponent();

    Table table = new Table();
    Field field = new Field(component.getGenerateFieldType());


    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    String str = "Родительское ид, " + component.getTable() + "_parent_id," + table.getSchema().getName() + "." + component.getTable() + "." + component.getTable() + Table.POSTFIX_TABLE_ID;
    field.initFieldByConfigTable(table, str, component);

    field.initFieldByConfigTable(table, component.getColumnsStr(), component);

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createFileField(FtlComponent ftlComponent) {
    Table table = new Table();
    BaseFile component = ftlComponent.getComponent();
    Field fileDataField = new Field(GenerateFieldType.FILEUPLOAD_FILEDATA);
    Field fileNameField = new Field(GenerateFieldType.FILEUPLOAD_FILENAME);

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());

    fileNameField.initField(table, FileUploadComponentHelper.getNameColumn(component.getField()), component);
    fileDataField.initField(table, FileUploadComponentHelper.getDataColumn(component.getField()), component);

    if (!fileDataField.isEmpty()) {
      table.setField(fileDataField);
      table.setField(fileNameField);
    }

    if (component instanceof FileUpload) {
      boolean isRequired = ((FileUpload) component).getRequireRule().isUnconditionallyRequired();
      fileDataField.setObligatory(isRequired);
      fileNameField.setObligatory(isRequired);
    }

    if (component.isSignRequired()) {
      Field signField = new Field(GenerateFieldType.TEXT);
      signField.initField(table, component.getSignColumnName(), component);
      if (component instanceof FileUpload) {
        signField.setObligatory(((FileUpload) component).getRequireRule().isUnconditionallyRequired());
      }
      table.setField(signField);
    }

    return table;
  }

  public static Table createAutocompleteLinkTable(FtlComponent ftlComponent) {
    Autocomplete component = ftlComponent.getComponent();

    String linkTableSchema = component.getRelatedLinkTable().getSchema();
    String mainTableSchema = component.getColumnReference().getSchema();
    String referencedTableSchema = component.getRelatedTableReference().getSchema();

    Table linkTable = new Table();
    linkTable.setSchema(new Schema(linkTableSchema));
    linkTable.setName(component.getRelatedLinkTable().getTable());

    Table mainTable = new Table();
    mainTable.setSchema(new Schema(mainTableSchema));
    mainTable.setName(component.getColumnReference().getTable());

    Table referencedTable = new Table();
    referencedTable.setSchema(new Schema(referencedTableSchema));
    referencedTable.setName(component.getRelatedTableReference().getTable());

    Field mainField = new Field();
    mainField.setName(component.getColumnReference().getTable().concat(Table.POSTFIX_TABLE_ID));
    mainField.setType(GenerateFieldType.BIGINT);
    mainField.setReferences(new References(linkTable, mainTable, mainField));

    Field referencedField = new Field();
    referencedField.setName(component.getRelatedTableReference().getTable().concat(Table.POSTFIX_TABLE_ID));
    referencedField.setType(GenerateFieldType.BIGINT);
    referencedField.setReferences(new References(linkTable, referencedTable, referencedField));

    linkTable.setField(mainField);
    linkTable.setField(referencedField);

    return linkTable;
  }

  public static Table createHiddenInputField(FtlComponent ftlComponent) {
    Table table = new Table();
    Field field = new Field(GenerateFieldType.VARCHAR);
    HiddenInput component = ftlComponent.getComponent();

    if (component.isCurrentTimeAsDefaultValue())
      field.setType(GenerateFieldType.TIMESTAMP_WITHOUT_TIME_ZONE);
    else if (!component.isCurrentUserAsDefaultValue())
      field.createReferences(table, component.getColumnReference());

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    field.initField(table, component.getField(), component);

    if (!field.isEmpty())
      table.setField(field);
    return table;
  }

  public static Table createAddressField(FtlComponent ftlComponent) {
    Table table = new Table();
    FiasAddress component = ftlComponent.getComponent();

    Field field = new Field(component.getGenerateFieldType());
    field.setFiasAddressColumn(true);
    Field dateField = new Field(GenerateFieldType.DATE);
    Field fullAddressField = new Field(GenerateFieldType.VARCHAR);

    String schemaName = component.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(component.getTable());
    field.initField(table, component.getField(), component);
    field.setObligatory(component.getRequireRule().isUnconditionallyRequired());
    if (!component.getUsageActual()) {
      dateField.initField(table, component.getDateField(), component);
      fullAddressField.initField(table, component.getFullField(), component);
    }

    if (!field.isEmpty()) {
      table.setField(field);
      if (!component.getUsageActual()) {
        table.setField(dateField);
        table.setField(fullAddressField);
      }
    }
    return table;
  }

  public static Table createTable(ColumnReference source, ColumnReference fkReference, GenerateFieldType fieldType) {
    Table table = new Table();
    table.setSchema(new Schema(source.getSchema()));
    table.setName(source.getTable());
    Field field = new Field(fieldType);
    field.setName(source.getColumn());
    table.setField(field);

    if (fkReference != null) {
      field.createReferences(table, fkReference);
    }

    return table;
  }

  public static Table createSphinxSearch(FtlComponent ftlComponent) {
    SphinxSearch sphinxSearch = ftlComponent.getComponent();

    Table table = new Table();

    String schemaName = sphinxSearch.getSchema();
    table.setSchema(new Schema(schemaName));
    table.setName(sphinxSearch.getTable());

    for (SphinxSaveIdColumn sphinxSaveIdColumn : sphinxSearch.getColumnsId()) {
      StringBuilder builder = new StringBuilder();
      builder.append(sphinxSaveIdColumn.getReference().getSchema()).append(".").append(sphinxSaveIdColumn.getReference().getTable());
      Table t = new Table();
      t.setName(sphinxSaveIdColumn.getReference().getTable());
      t.setSchema(new Schema(sphinxSaveIdColumn.getReference().getSchema()));

      Field field = new Field(sphinxSearch.getGenerateFieldType());
      field.initField(table, sphinxSaveIdColumn.getColumnName(), sphinxSearch);
      field.setReferences(new References(table, t, field));
      table.setField(field);
    }
    return table;
  }
}
