package ru.cg.runaex.database.dao;

import org.springframework.dao.DataAccessException;

import ru.cg.runaex.database.bean.model.Category;
import ru.cg.runaex.database.bean.model.FiasColumn;
import ru.cg.runaex.database.bean.model.MetadataEditableTreeGrid;
import ru.cg.runaex.database.bean.model.ProcessDbConnection;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Петров А.
 */
public interface MetadataDao {

  void deleteProject(String categoryName);

  Long saveProject(String projectName);

  Long saveGroovyScript(String script);

  String getGroovyScript(Long id);

  Long saveCategory(String categoryName, Long parentCategoryId);

  void saveProcesses(List<String> processesNames, Long categoryId);

  List<Category> loadAllProjects();

  List<Category> loadProjectsWithInboxFilter(Set<Long> categoriesByTasks);

  Map<String, Long> loadAllProcessesCategoriesLinks();

  Map<String, Long> loadLinksWithInboxFilter(Set<String> businessProcessesByTasks);

  void deleteDbConnections(List<Long> deletedProcesses);

  void saveDbConnections(List<ProcessDbConnection> dbConnections);

  String getDbConnectionJndiName(Long processDefinitionId);

  List<Long> getAllProcessDefinitionsByDbConnections();

  void deleteExpiredGroovyScripts(String expiryDate);

  void deleteFiasColumns(List<Long> deletedProcesses);

  void saveFiasColumns(List<FiasColumn> fiasColumns);

  Set<String> getFiasColumns(Long currentProcessDefinitionId, String schemaName, String tableName);

  void insertProjectPredefinedGroovyFunctions(String code, String projectName);

  void deleteProjectPredefinedGroovyFunctions(String projectName);

  String loadProjectPredefinedGroovyFunctions(String projectName);

  ProcessDbConnection getProjectDatabaseConnectionInfo(Long processDefinitionId);

  MetadataEditableTreeGrid getEditableTreeGrid(String tableId);

  void saveOrUpdateEditableTreeGrid(MetadataEditableTreeGrid editableTreeGrid);

  void deleteSphinxData(List<Long> ids) throws DataAccessException;
}
