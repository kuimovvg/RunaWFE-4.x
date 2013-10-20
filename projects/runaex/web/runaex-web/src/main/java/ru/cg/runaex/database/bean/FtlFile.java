package ru.cg.runaex.database.bean;

import java.io.Serializable;

/**
 * @author Bagautdinov
 */
public class FtlFile implements Serializable {
  private static final long serialVersionUID = -2051101627266564924L;

  private String name;
  private String contents;

  public FtlFile(String name, String contents) {
    this.name = name;
    this.contents = contents;
  }

  public String getName() {
    return name;
  }

  public String getContents() {
    return contents;
  }
}
