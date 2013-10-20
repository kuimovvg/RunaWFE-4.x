package ru.cg.runaex.groovy.classimport;

/**
 * @author Абдулин Ильдар
 */
public class GroovyClassImport {
  private String alias;
  private String className;


  public GroovyClassImport(String alias, String className) {
    this.alias = alias;
    this.className = className;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Class loadClass() throws ClassNotFoundException {
    return getClass().getClassLoader().loadClass(getClassName());
  }
}
