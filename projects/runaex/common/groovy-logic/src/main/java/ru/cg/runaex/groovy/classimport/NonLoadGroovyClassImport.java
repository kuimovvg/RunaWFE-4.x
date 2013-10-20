package ru.cg.runaex.groovy.classimport;

/**
 * @author Абдулин Ильдар
 */
public class NonLoadGroovyClassImport extends GroovyClassImport {
  public NonLoadGroovyClassImport(String alias, String className) {
    super(alias, className);
  }

  @Override
  public Class loadClass() throws ClassNotFoundException {
    return null;
  }
}
