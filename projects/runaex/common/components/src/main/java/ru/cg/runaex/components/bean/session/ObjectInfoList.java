/*
 * Copyright (c) 2012.
 *
 * Class: ObjectInfoList
 * Last modified: 25.09.12 12:15
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.components.bean.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectInfoList implements Serializable {
  private static final long serialVersionUID = 261725394867998278L;
  private List<String> list;

  public ObjectInfoList() {
  }

  public ObjectInfoList(List<String> list) {
    this.list = list;
  }

  public void add(String objectInfo) {
    if (this.list == null)
      this.list = new ArrayList<String>();
    this.list.add(objectInfo);
  }

  public void setList(List<String> list) {
    this.list = list;
  }

  public List<String> getList() {
    if (this.list == null)
      this.list = new ArrayList<String>();
    return list;
  }
}
