package ru.cg.runaex.shared.bean.project.xml;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Kochetkov
 */
@XStreamAlias("groovyFunctionList")
public class GroovyFunctionList implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<GroovyFunction> groovyFunctionList = new LinkedList<GroovyFunction>();

  public List<GroovyFunction> getGroovyFunctionList() {
    return groovyFunctionList;
  }

  public void setGroovyFunctionList(List<GroovyFunction> groovyFunctionList) {
    this.groovyFunctionList = groovyFunctionList;
  }
}
