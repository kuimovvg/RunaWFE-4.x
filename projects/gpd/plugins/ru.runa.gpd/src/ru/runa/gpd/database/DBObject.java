package ru.runa.gpd.database;

import java.io.Serializable;

import org.apache.ddlutils.model.Database;

/**
 * @author Kochetkov
 */
public class DBObject implements Serializable {
  private static final long serialVersionUID = 2871787924222672486L;
  private String name;
  private Database model;

  public DBObject(String name, Database model) {
    this.name = name;
    this.model = model;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Database getModel() {
    return model;
  }

  public void setModel(Database model) {
    this.model = model;
  }
}
