package ru.cg.runaex.components.bean.session;

import java.io.Serializable;

import ru.cg.runaex.components.WfeRunaVariables;

/**
 * @author Sabirov
 */
public class ObjectInfo implements Serializable {
  private static final long serialVersionUID = 261725394867998278L;
  private Long id;
  private Long parentId;
  private String schema;
  private String table;
  private String selectTreeGridfield;
  private ObjectInfo base;
  private ObjectInfo attachable;

  public ObjectInfo() {
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public void setSelectTreeGridfield(String selectTreeGridfield) {
    this.selectTreeGridfield = selectTreeGridfield;
  }

  public void setBase(ObjectInfo base) {
    this.base = base;
  }

  public void setAttachable(ObjectInfo attachable) {
    this.attachable = attachable;
  }

  public Long getParentId() {
    return parentId;
  }

  public Long getId() {
    return id;
  }

  public String getSchema() {
    return schema;
  }

  public String getTable() {
    return table;
  }

  public ObjectInfo getBase() {
    return base;
  }

  public ObjectInfo getAttachable() {
    return attachable;
  }

  public String getSelectTreeGridfield() {
    return selectTreeGridfield;
  }

  public boolean isEmpty() {
    return WfeRunaVariables.isEmpty(schema) && WfeRunaVariables.isEmpty(table) &&
        id == null && parentId == null &&
        (base == null || base.isEmpty()) && (attachable == null || attachable.isEmpty());
  }

  /**
   * !!!No change!!!
   *
   * @return schema.table
   */
  @Override
  public String toString() {
    return schema + "." + table;
  }
}
