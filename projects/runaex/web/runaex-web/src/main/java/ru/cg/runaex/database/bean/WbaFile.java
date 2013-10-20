package ru.cg.runaex.database.bean;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import ru.cg.runaex.shared.bean.project.xml.GroovyFunctionList;
import ru.cg.runaex.shared.bean.project.xml.Project;

/**
 * @author urmancheev
 */
public class WbaFile implements Serializable {
  private static final long serialVersionUID = -8856599967137098733L;

  private List<ParFile> parFiles = new LinkedList<ParFile>();
  private Project projectStructure;
  private String jndiName;
  private String jdbcDriverClassName;
  private GroovyFunctionList projectGroovyFunctions;

  public void addParFile(ParFile parFile) {
    parFiles.add(parFile);
  }

  public List<ParFile> getParFiles() {
    return parFiles;
  }

  public Project getProjectStructure() {
    return projectStructure;
  }

  public void setProjectStructure(Project projectStructure) {
    this.projectStructure = projectStructure;
  }

  public String getJndiName() {
    return jndiName;
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public GroovyFunctionList getProjectGroovyFunctions() {
    return projectGroovyFunctions;
  }

  public void setProjectGroovyFunctions(GroovyFunctionList projectGroovyFunctions) {
    this.projectGroovyFunctions = projectGroovyFunctions;
  }

  public String getJdbcDriverClassName() {
    return jdbcDriverClassName;
  }

  public void setJdbcDriverClassName(String jdbcDriverClassName) {
    this.jdbcDriverClassName = jdbcDriverClassName;
  }
}
