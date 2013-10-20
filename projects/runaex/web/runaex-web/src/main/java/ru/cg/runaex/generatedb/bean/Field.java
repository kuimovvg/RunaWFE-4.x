package ru.cg.runaex.generatedb.bean;

import java.text.MessageFormat;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.components.bean.component.IsComponent;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.generatedb.GenerateDBImpl;

/**
 * @author Sabirov
 */
public class Field extends GenerateDBImpl {
  /**
   * Первичный ключ
   */
  private boolean isPk;
  /**
   * Наименование колонки
   */
  private String name;
  /**
   * Тип колонки
   */
  private GenerateFieldType type;
  /**
   * Длина колонки
   */
  private Integer length;
  /**
   * Клмментарий к колонке
   */
  private String comment;
  /**
   * Обязательность
   */
  private boolean obligatory;
  /**
   * Ссылки
   */
  private References references;
  /**
   * Регулярное выражение
   */
  private String regex;

  private boolean isInitTypeFromComp = false;
  /**
   * create field from component table
   */
  private boolean isCreateFromCompTable = false;

  /**
   * this is true when it is fias column
   */
  private boolean isFiasAddressColumn = false;

  public Field() {
  }

  public Field(GenerateFieldType strGenerateFieldType) {
    this();
    GenerateFieldType fieldType = strGenerateFieldType == null ? GenerateFieldType.VARCHAR : strGenerateFieldType;
    switch (fieldType) {
      case VARCHAR:
        setLength(Table.DEFAULTS_FIELD_LENGTH);
        break;
      case BOOLEAN:
      case BIGINT:
      case NUMERIC:
      case INTEGER:
      case TIMESTAMP_WITHOUT_TIME_ZONE:
      case DATE:
      case FILEUPLOAD_FILEDATA:
      case FILEUPLOAD_FILENAME:
        break;
    }
    setType(fieldType);
    setInitTypeFromComp(true);
  }

  public static Field[] createEmptyObjects(int size) {
    return new Field[size];
  }

  public boolean isPk() {
    return isPk;
  }

  public void setPk(boolean pk) {
    isPk = pk;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public GenerateFieldType getType() {
    return type;
  }

  public void setType(GenerateFieldType type) {
    this.type = type;
    this.setInitTypeFromComp(false);
  }

  public Integer getLength() {
    return length != null ? length : 0;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public boolean isObligatory() {
    return obligatory;
  }

  public void setObligatory(boolean obligatory) {
    this.obligatory = obligatory;
  }

  public References getReferences() {
    return references;
  }

  public void setReferences(References references) {
    this.references = references;
  }

  public String getRegex() {
    return regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public boolean isInitTypeFromComp() {
    return isInitTypeFromComp;
  }

  public void setInitTypeFromComp(boolean initTypeFromComp) {
    this.isInitTypeFromComp = initTypeFromComp;
  }

  public boolean isCreateFromCompTable() {
    return isCreateFromCompTable;
  }

  public void setCreateFromCompTable(boolean createFromCompTable) {
    isCreateFromCompTable = createFromCompTable;
  }

  public boolean isFiasAddressColumn() {
    return isFiasAddressColumn;
  }

  public void setFiasAddressColumn(boolean fiasAddressColumn) {
    isFiasAddressColumn = fiasAddressColumn;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if ((obj == null) || (obj.getClass() != this.getClass()))
      return false;

    Field field = (Field) obj;
    return getName() != null && getName().equals(field.getName());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + (getName() != null ? getName().hashCode() : "field".hashCode());
    return hash;
  }

  @Override
  public String toString() {
    return "Field properties: isPk - " + isPk() + ", name - " + getName() + ", type - " + getType() + ", length - " + getLength() + ", " +
        "comment - " + getComment() + ", - obligatory " + isObligatory() + ", regex - " + getRegex() + ", \n " +
        (references != null ? references.toString() : "null");
  }

  @Override
  public String getSQL() {

    String sqlType = "";
    if (getType() != null && !isPk()) {
      switch (getType()) {
        case VARCHAR:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.VARCHAR.toString()), String.valueOf(getLength()));
          break;
        case TEXT:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.TEXT.toString()),"");
          break;
        case INTEGER:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.INTEGER.toString()), "");
          break;
        case BIGINT:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.BIGINT.toString()), "");
          break;
        case NUMERIC:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.NUMERIC.toString()), "");
          break;
        case BOOLEAN:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.BOOLEAN.toString()), "");
          break;
        case TIMESTAMP_WITHOUT_TIME_ZONE:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.TIMESTAMP_WITHOUT_TIME_ZONE.toString()), "");
          break;
        case DATE:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.DATE.toString()), "");
          break;
//        case BIGDECIMAL:
//          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.BIGDECIMAL.toString()), "");
//          break;
        case FILEUPLOAD_FILEDATA:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.FILEUPLOAD_FILEDATA.toString()), "");
          break;
        case FILEUPLOAD_FILENAME:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.FILEUPLOAD_FILENAME.toString()), "");
          break;
        case VARCHAR_ARRAY:
          sqlType = MessageFormat.format(getSQLTemplate(GenerateFieldType.VARCHAR_ARRAY.toString()), "");
          break;
      }
    }

    String template = super.getSQL();
    return MessageFormat.format(template, getName(), sqlType, isObligatory() ? getSQLTemplate("NOT_NULL") : "");
  }

  /**
   * Check empty field by name
   *
   * @return true if field name is empty
   */
  public boolean isEmpty() {
    return this.getName() == null || this.getName().isEmpty();
  }

  /**
   * Copy values
   *
   * @param copyFromField copy values from field
   */
  protected void copy(Field copyFromField) {
    if (!this.isPk() && copyFromField.isPk())
      this.setPk(copyFromField.isPk());
    if ((this.getType() == null && copyFromField.getType() != null) ||
        (!this.isInitTypeFromComp() && copyFromField.isInitTypeFromComp()) ||
        (this.isCreateFromCompTable() && !copyFromField.isCreateFromCompTable()))
      this.setType(copyFromField.getType());
    if ((this.getLength() == null && copyFromField.getLength() != null) ||
        (!this.isInitTypeFromComp() && copyFromField.isInitTypeFromComp()) ||
        (this.isCreateFromCompTable() && !copyFromField.isCreateFromCompTable()))
      this.setLength(copyFromField.getLength());
    if ((this.getComment() == null || this.getComment().isEmpty()) &&
        (copyFromField.getComment() != null && !copyFromField.getComment().isEmpty()))
      this.setComment(copyFromField.getComment());
    if (!this.isObligatory() && copyFromField.isObligatory())
      this.setObligatory(copyFromField.isObligatory());
    if ((this.getRegex() == null || this.getRegex().isEmpty()) &&
        (copyFromField.getRegex() != null && !copyFromField.getRegex().isEmpty()))
      this.setRegex(copyFromField.getRegex());
    if (this.getReferences() == null && copyFromField.getReferences() != null)
      this.setReferences(copyFromField.getReferences());
    this.setFiasAddressColumn(copyFromField.isFiasAddressColumn());
  }

  /**
   * Create reference in field
   *
   * @param table - table
   */
  protected void createReferences(Table table, ColumnReference columnReference) {
    if (columnReference != null) {
      Table refTable = new Table();
      String refSchemaName = columnReference.getSchema();

      refTable.setSchema(new Schema(refSchemaName));
      refTable.setName(columnReference.getTable());
      Field refField = new Field();
      refField.setName(columnReference.getColumn());
      refField.setType(GenerateFieldType.VARCHAR);
      refField.setLength(Table.DEFAULTS_FIELD_LENGTH);
      refTable.setField(refField);
      setType(GenerateFieldType.BIGINT);
      setLength(null);
      setReferences(new References(table, refTable, this));
    }
  }

  /**
   * Initialization field data
   * <p/>
   * todo remove parsing - we already have all data in component
   */
  protected void initFieldByConfigTable(Table table, String configField, IsComponent component) {
    if (configField == null || configField.isEmpty()) {
      return;
    }

    String[] newFieldNames = configField.split(";");
    for (String newFieldName : newFieldNames) {
      Field newField = new Field();
      String[] tmp = newFieldName.split(",");
      String displayName = "";
      String fieldName = "";
      String referencesField = "";
      if (tmp.length == 1) {
        fieldName = tmp[0].trim();
      }
      else if (tmp.length == 2) {
        displayName = tmp[0].trim();
        fieldName = tmp[1].trim();
      }
      else if (tmp.length == 3) {
        displayName = tmp[0].trim();
        fieldName = tmp[1].trim();
        referencesField = tmp[2].trim();
      }

      newField.setName(fieldName);
      newField.setComment(displayName);
      newField.setCreateFromCompTable(true);
      /**
       * add field type BIGINT if field contains schema and table name
       */
      if (referencesField.split(UnicodeSymbols.POINT).length > 1) {
        newField.setType(GenerateFieldType.BIGINT);
        newField.setLength(null);
        ColumnReference reference = ComponentParser.parseColumnReference(referencesField, component.getDefaultSchema());
        newField.createReferences(table, reference);
      }
      else {
        newField.setType(GenerateFieldType.VARCHAR);
        newField.setLength(Table.DEFAULTS_FIELD_LENGTH);
      }
      if (!newField.isEmpty())
        table.setField(newField);
    }
  }

  /**
   * Initialization field data
   * <p/>
   * todo remove parsing - we already have all data in component
   */
  protected void initField(Table table, String configField, IsComponent component) {
    String[] tmp = configField.split(",");
    String displayName = "";
    String fieldName = "";
    String referencesField = "";
    /**
     * exist filed name
     */
    if (tmp.length == 1) {
      fieldName = tmp[0].trim();
    }
    /**
     * exist Display name and filed name
     */
    else if (tmp.length == 2) {
      displayName = tmp[0].trim();
      fieldName = tmp[1].trim();
    }
    /**
     * exist Display name, filed name and references
     */
    else if (tmp.length == 3) {
      displayName = tmp[0].trim();
      fieldName = tmp[1].trim();
      referencesField = tmp[2].trim();
    }
    setName(fieldName);
    setComment(displayName);
    if (referencesField.split(UnicodeSymbols.POINT).length > 1) {
      setType(GenerateFieldType.BIGINT);
      setLength(null);
      ColumnReference reference = ComponentParser.parseColumnReference(referencesField, component.getDefaultSchema());
      createReferences(table, reference);
    }
  }
}