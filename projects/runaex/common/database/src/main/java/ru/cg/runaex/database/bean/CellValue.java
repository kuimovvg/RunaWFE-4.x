package ru.cg.runaex.database.bean;

/**
 * @author Абдулин Ильдар
 */
public class CellValue {

  private Object value;
  private Cell cell;

  public CellValue() {

  }

  public CellValue(Cell cell, Object value) {
    this.value = value;
    this.cell = cell;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Cell getCell() {
    return cell;
  }

  public void setCell(Cell cell) {
    this.cell = cell;
  }
}
