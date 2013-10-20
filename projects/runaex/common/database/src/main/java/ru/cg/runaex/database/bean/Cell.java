package ru.cg.runaex.database.bean;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.*;

public class Cell {


  public static class CellDeserializer implements JsonDeserializer<Cell> {
    @Override
    public Cell deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
      return new Cell(jsonElement.getAsJsonPrimitive().getAsString());
    }
  }

  public static class CellSerializer implements JsonSerializer<Cell> {

    public JsonElement serialize(Cell cell, Type type, JsonSerializationContext jsonSerializationContext) {
      if (cell != null)
        return new JsonPrimitive(cell.toString());
      else
        return new JsonPrimitive("");
    }
  }

  public static final Pattern cellPattern = Pattern.compile("cell\\[(\\d+)\\]\\[([^\\]]+)\\]");
  private Long id;
  private String column;

  private String alias;

  public Cell() {

  }

  public Cell(Long id, String column) {
    this.id = id;
    column = column.trim();
    this.column = createColumn(column);
    createAlias();
  }

  public Cell(Long id, String column, boolean useOriginalColumn) {
    this.id = id;
    column = column.trim();
    if (!useOriginalColumn)
      this.column = createColumn(column);
    else
      this.column = column;
    createAlias();
  }

  public Cell(String cell) {
    Matcher matcher = cellPattern.matcher(cell);
    if (matcher.find()) {
      this.id = Long.valueOf(matcher.group(1));
      this.column = createColumn(matcher.group(2).trim());
    }
    createAlias();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
    createAlias();
  }

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    column = column.trim();
    this.column = createColumn(column);
    createAlias();
  }

  protected String createColumn(String column) {
    //todo фигня какая-то, сделано на скорую руку. переделать!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    String[] tmp = column.split(":");
    if (tmp.length == 1) {
      return column;
    }
    else {
      if (tmp.length > 3) {
        String schema = tmp[0];
        String table = tmp[2];
        column = tmp[3];
        return schema + "." + table + "." + column;
      }
      else {
        return tmp[2];
      }
    }
  }

  protected void createAlias() {
    alias = "cell_" + id + "_" + getMd5(column);
  }


  protected String getMd5(String md5) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] array = md.digest(md5.getBytes());
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
      }
      return sb.toString();
    }
    catch (NoSuchAlgorithmException e) {
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof Cell)) {
      return false;
    }
    Cell cell = (Cell) obj;
    return id.equals(cell.id) && column.equals(cell.column);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (column != null ? column.hashCode() : 0);
    return result;
  }

  public String getAlias() {
    return alias;
  }

  public String toString() {
    return "cell[" + id + "][" + column + "]";
  }
}