package ru.cg.runaex.web.service;

import ru.cg.runaex.database.bean.model.MetadataEditableTreeGrid;
import ru.cg.runaex.shared.bean.project.xml.GroovyFunctionList;
import ru.cg.runaex.shared.bean.project.xml.Project;

import ru.cg.runaex.exceptions.SphinxException;
import ru.cg.runaex.generatedb.bean.Table;
import ru.cg.runaex.generatedb.util.TableHashSet;
import ru.cg.runaex.web.model.DeployedBusinessApplication;

/**
 * @author urmancheev
 */
public interface DatabaseMetadataService {

  void saveMetadata(DeployedBusinessApplication processes, String dataSourceJndiName) throws SphinxException;

  void saveProjectStructure(Project project);

  void updateDbConnections(DeployedBusinessApplication deployedBusinessApplication, String dataSourceJndiName, String driverClassName);

  void saveProjectGroovyFunction(GroovyFunctionList groovyFunctionList, String projectName);

  void saveCascadePreferences(TableHashSet<Table> tables);

  MetadataEditableTreeGrid getEditableTreeGrid(String tableId);
}
