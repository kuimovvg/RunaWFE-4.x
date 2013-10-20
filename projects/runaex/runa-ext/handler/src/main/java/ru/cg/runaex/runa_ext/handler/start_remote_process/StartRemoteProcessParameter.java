package ru.cg.runaex.runa_ext.handler.start_remote_process;

import java.io.Serializable;

/**
 * @author urmancheev
 */
public class StartRemoteProcessParameter implements Serializable {
  private static final long serialVersionUID = -2607270999442848766L;

  private String name;
  private String source;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
