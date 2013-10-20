package ru.cg.runaex.web.service;

import com.cg.jul.core.net.JndiHostPortSource;
import com.cg.sphinx_tools.client.STFactory;
import com.cg.sphinx_tools.core.SphinxTools;
import com.cg.sphinx_tools.core.bean.DBConnection;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.cg.runaex.components.ConnectionInfo;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.SphinxSearch;
import ru.cg.runaex.components.bean.component.field.FiasAddress;
import ru.cg.runaex.components.bean.component.grid.EditableTreeGrid;
import ru.cg.runaex.components.bean.component.part.SphinxIndexingColumn;
import ru.cg.runaex.components.bean.component.part.SphinxViewColumn;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.database.bean.DependencyMatrix;
import ru.cg.runaex.database.bean.FtlComponent;
import ru.cg.runaex.database.bean.FullTableParam;
import ru.cg.runaex.database.bean.model.FiasColumn;
import ru.cg.runaex.database.bean.model.MetadataEditableTreeGrid;
import ru.cg.runaex.database.bean.model.ProcessDbConnection;
import ru.cg.runaex.shared.bean.project.xml.*;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.SaveTransportData;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.database.dao.SaveDao;
import ru.cg.runaex.database.util.NetworkUtil;
import ru.cg.runaex.exceptions.SphinxException;
import ru.cg.runaex.generatedb.bean.Field;
import ru.cg.runaex.generatedb.bean.Schema;
import ru.cg.runaex.generatedb.bean.Table;
import ru.cg.runaex.generatedb.util.TableHashSet;
import ru.cg.runaex.groovy.cache.GroovyScriptExecutorCache;
import ru.cg.runaex.shared.bean.project.xml.Process;
import ru.cg.runaex.web.model.DeployedBusinessApplication;
import ru.cg.runaex.web.model.DeployedProcess;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * @author urmancheev
 */
@Service
public class DatabaseMetadataServiceImpl implements DatabaseMetadataService {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseMetadataServiceImpl.class);

  private String targetDbPort;
  private String targetDbHost;

  @Autowired
  private BaseDao baseDao;
  @Autowired
  private SaveDao saveDao;
  @Autowired
  private MetadataDao metadataDao;

  @Autowired
  private ConnectionInfo connectionInfo;
  @Autowired
  @Qualifier("sphinxToolsDataSource")
  private JndiHostPortSource sphinxToolsDS;

  @Autowired
  private GroovyScriptExecutorCache groovyScriptExecutorCache;

  @PostConstruct
  private void init() {
    targetDbPort = connectionInfo.getDbUrl().replaceAll("jdbc:postgresql://.*\\:", "");
    targetDbHost = connectionInfo.getDbUrl().replaceAll("jdbc:.*://", "").replaceAll(":\\d*", "");
  }

  @Override
  public void saveMetadata(DeployedBusinessApplication application, String dataSourceJndiName) throws SphinxException {
    List<FtlComponent> components = new LinkedList<FtlComponent>();
    for (DeployedProcess process : application.getProcesses()) {
      components.addAll(process.getParFile().getFtlComponents());
    }
    saveFiasColumns(application, dataSourceJndiName);
    saveSphinxSearchData(components);
    generateSphinxIndexes(components);
    saveEditableTreeGrid(components);
  }

  private void saveEditableTreeGrid(List<FtlComponent> components) {
    for (FtlComponent ftlComponent : components) {
      if (ftlComponent.getComponentType() == ComponentType.EDITABLE_TREE_GRID) {
        MetadataEditableTreeGrid editableTreeGrid = metadataDao.getEditableTreeGrid(ftlComponent.<EditableTreeGrid>getComponent().getTableId());
        if (editableTreeGrid == null) {
          editableTreeGrid = new MetadataEditableTreeGrid();
        }
        //todo
        editableTreeGrid.setDependencyMatrix(DependencyMatrix.create(ftlComponent.<EditableTreeGrid>getComponent().getBusinessRules(),new FullTableParam(ftlComponent.<EditableTreeGrid>getComponent().getSchema(),ftlComponent.<EditableTreeGrid>getComponent().getTable(),ftlComponent.<EditableTreeGrid>getComponent().getColumnsStr().split(","),ftlComponent.<EditableTreeGrid>getComponent().getLinkColumnsStr(),ftlComponent.<EditableTreeGrid>getComponent().getParentColumn(),ftlComponent.<EditableTreeGrid>getComponent().getTableId())));
        editableTreeGrid.setEditableRule(ComponentUtil.replaceCharacters(ftlComponent.<EditableTreeGrid>getComponent().getEditableRule()));
        editableTreeGrid.setTableId(ftlComponent.<EditableTreeGrid>getComponent().getTableId());
        editableTreeGrid.setCssClass(ftlComponent.<EditableTreeGrid>getComponent().getCssClass());
        metadataDao.saveOrUpdateEditableTreeGrid(editableTreeGrid);
      }
    }
  }

  private void saveFiasColumns(DeployedBusinessApplication application, String dataSourceJndiName) {
    if (!application.getOldProcessDefinitionIds().isEmpty())
      metadataDao.deleteFiasColumns(application.getOldProcessDefinitionIds());

    List<FiasColumn> fiasColumns = getFiasColumns(application, dataSourceJndiName);
    if (!fiasColumns.isEmpty())
      metadataDao.saveFiasColumns(fiasColumns);
  }

  private List<FiasColumn> getFiasColumns(DeployedBusinessApplication application, String dataSourceJndiName) {
    List<FiasColumn> fiasColumns = new LinkedList<FiasColumn>();

    for (DeployedProcess process : application.getProcesses()) {
      LinkedList<Table> tables = new LinkedList<Table>();

      //Группируем по table
      for (FtlComponent component : process.getParFile().getFtlComponents()) {
        if (component.getComponentType() == ComponentType.FIAS_ADDRESS && component.<FiasAddress>getComponent().getUsageActual()) {
          Table table = new Table();
          table.setSchema(new Schema(component.<FiasAddress>getComponent().getSchema()));
          table.setName(component.<FiasAddress>getComponent().getTable());
          Field field = new Field();
          field.setName(component.<FiasAddress>getComponent().getField());
          if (!tables.contains(table)) {
            table.setField(field);
            tables.add(table);
          }
          else {
            tables.get(tables.indexOf(table)).setField(field);
          }
        }
      }

      for (Table table : tables) {
        String[] fields = new String[table.getFields().size()];
        int i = 0;
        for (Field field : table.getFields()) {
          fields[i] = field.getName();
          i++;
        }
        FiasColumn fiasColumn = new FiasColumn();
        fiasColumn.setProcessId(process.getDefinitionId());
        fiasColumn.setDataSourceJndiName(dataSourceJndiName);
        fiasColumn.setTable(table.getSchema().getName() + "." + table.getName());
        fiasColumn.setFields(fields);
        fiasColumns.add(fiasColumn);
      }
    }

    return fiasColumns;
  }

  @Override
  public void saveCascadePreferences(TableHashSet<Table> tables) {
    if (tables.isEmpty()) {
      return;
    }
    //delete previous
    List<Long> idsForDeletion = new LinkedList<Long>();
    String[] selectFields = new String[1];
    selectFields[0] = "cascade_fks_id";
    List<String> tableNames = new ArrayList<String>();
    for (Table table : tables) {
      tableNames.add(table.getSchema().getName().concat(".").concat(table.getName()));
    }
    List<Data> dataList = new ArrayList<Data>(1);
    dataList.add(new Data("table_name_in", tableNames, "varchar"));
    TransportData filterData = new TransportData(0, dataList);
    TransportDataSet resultData = baseDao.getData(null, "metadata", "cascade_fks", selectFields, null, null, null, null,
        null, null, filterData, "dummy", null, null);
    for (TransportData td : resultData.getSets()) {
      idsForDeletion.add((Long) td.getData("cascade_fks_id").getValue());
    }
    if (!idsForDeletion.isEmpty()) {
      SaveTransportData deleteTransportData = new SaveTransportData(null, null, "metadata", "cascade_fks", null);
      deleteTransportData.setIds(idsForDeletion);
      baseDao.deleteData(null, deleteTransportData);
    }

    SaveTransportData saveTransportData;
    for (Table table : tables) {
      List<String> fieldsWithCascadeDeletion = new LinkedList<String>();
      for (Field field : table.getFields()) {
        if (field.getReferences() != null && field.getReferences().isCascadeDeletion())
          fieldsWithCascadeDeletion.add(field.getName());
      }
      if (!fieldsWithCascadeDeletion.isEmpty()) {
        dataList = new LinkedList<Data>();
        dataList.add(new Data("table_name", table.getSchema().getName() + "." + table.getName(), String.class.toString()));
        dataList.add(new Data("fields", fieldsWithCascadeDeletion, Collection.class.toString()));
        saveTransportData = new SaveTransportData(null, null, "metadata", "cascade_fks", dataList);
        saveDao.saveData(null, saveTransportData);
      }
    }
  }

  @Override
  public MetadataEditableTreeGrid getEditableTreeGrid(String tableId) {
    return metadataDao.getEditableTreeGrid(tableId);
  }

  private void saveSphinxSearchData(List<FtlComponent> components) {
    for (FtlComponent ftlComponent : components) {
      if (ComponentType.SPHINX_SEARCH == ftlComponent.getComponentType()) {
        SphinxSearch sphinxSearch = ftlComponent.getComponent();
        if (sphinxSearch.isUseExistIndex()) {
          continue;
        }

        List<String> usingTables = new ArrayList<String>();
        for (SphinxViewColumn viewColumn : sphinxSearch.getViewColumns()) {
          String tableName = viewColumn.getReference().getSchema() + "." + viewColumn.getReference().getTable();
          if (!usingTables.contains(tableName)) {
            usingTables.add(tableName);
          }
        }

        for (SphinxIndexingColumn indexingColumn : sphinxSearch.getIndexColumns()) {
          String tableName = indexingColumn.getReference().getSchema() + "." + indexingColumn.getReference().getTable();
          if (!usingTables.contains(tableName)) {
            usingTables.add(tableName);
          }
        }
        String indexName = sphinxSearch.getIndexName();
        String[][] sphinxData = createSphinxData(sphinxSearch);
        String[] viewColumns = sphinxData[0];
        String[] viewTableIds = sphinxData[1];
        String[] isIdArray = sphinxData[2];

        String[] allViewColumns = new String[viewTableIds.length + sphinxSearch.getViewColumns().size()];
        System.arraycopy(viewColumns, 0, allViewColumns, 0, sphinxSearch.getViewColumns().size());
        System.arraycopy(viewTableIds, 0, allViewColumns, sphinxSearch.getViewColumns().size(), viewTableIds.length);
        for (int i = 0; i < allViewColumns.length; i++) {
          String[] arr = allViewColumns[i].split("\\.");
          allViewColumns[i] = new StringBuilder().append(arr[0].charAt(0)).append(".").append(arr[1].charAt(0)).append(".").append(arr[2]).toString();
        }

        //Удаление
        List<Long> idsForDeletion = new LinkedList<Long>();
        String[] selectFields = new String[1];
        selectFields[0] = "sphinx_search_id";
        List<Data> dataList = new ArrayList<Data>(1);
        dataList.add(new Data("index_in", indexName, "varchar"));
        TransportData filterData = new TransportData(0, dataList);
        TransportDataSet resultData = baseDao.getData(null, "metadata", "sphinx_search", selectFields, null, null, null, null,
            null, null, filterData, "dummy", null, null);
        for (TransportData td : resultData.getSets()) {
          idsForDeletion.add((Long) td.getData("sphinx_search_id").getValue());
        }
        if (!idsForDeletion.isEmpty()) {
          metadataDao.deleteSphinxData(idsForDeletion);
        }

        //Сохранение
        dataList = new LinkedList<Data>();
        dataList.add(new Data("index", indexName, String.class.toString()));
        dataList.add(new Data("columns", Arrays.asList(allViewColumns), Collection.class.toString()));
        dataList.add(new Data("using_tables", usingTables, Collection.class.toString()));
        dataList.add(new Data("is_id", Arrays.asList(isIdArray), Collection.class.toString()));
        dataList.add(new Data("indexing_columns", sphinxSearch.getIndexingColumnsStr(), String.class.toString()));
        dataList.add(new Data("view_columns", sphinxSearch.getViewColumnsStr(), String.class.toString()));
        SaveTransportData saveTransportData = new SaveTransportData(null, null, "metadata", "sphinx_search", dataList);
        saveDao.saveData(null, saveTransportData);
      }
    }
  }

  private void generateSphinxIndexes(List<FtlComponent> components) throws SphinxException {
    boolean foundSphinxSearchComponent = false;
    SphinxTools st = null;
    for (FtlComponent ftlComponent : components) {
      if (ComponentType.SPHINX_SEARCH == ftlComponent.getComponentType()) {
        foundSphinxSearchComponent = true;
      }
    }

    if (foundSphinxSearchComponent) {
      st = STFactory.create(sphinxToolsDS);
      st.deleteAllIndexes();
    }

    for (FtlComponent ftlComponent : components) {
      if (ComponentType.SPHINX_SEARCH == ftlComponent.getComponentType()) {
        if (NetworkUtil.isLocalhost(targetDbHost)) {
          throw new SphinxException("localhost is not valid address for the database");
        }
        SphinxSearch m = ftlComponent.getComponent();
        if (m.isUseExistIndex()) {
          continue;
        }
        String indexName = m.getIndexName();
        String[] indexingColumns = new String[m.getIndexColumns().size()];
        int index = 0;
        for (SphinxIndexingColumn indexingColumn : m.getIndexColumns()) {
          indexingColumns[index++] = indexingColumn.getReference().toString();
        }

        String[][] sphinxData = createSphinxData(m);
        String[] viewColumns = sphinxData[0];
        String[] viewTableIds = sphinxData[1];

        for (int i = 0; i < viewTableIds.length; i++) {
          viewTableIds[i] = "UINT:" + viewTableIds[i];
        }

        String[] allViewColumns = new String[viewTableIds.length + viewColumns.length];
        System.arraycopy(viewColumns, 0, allViewColumns, 0, viewColumns.length);
        System.arraycopy(viewTableIds, 0, allViewColumns, viewColumns.length, viewTableIds.length);

        try {
          st.createDeltaAndFullIndex(new DBConnection(DBConnection.DBType.POSTGRES_SQL, targetDbHost, targetDbPort, connectionInfo.getTargetDbUsername(), connectionInfo.getTargetDbPassword(), connectionInfo.getTargetDbName()), indexName, indexingColumns, allViewColumns);
        }
        catch (Exception ex) {
          logger.error(ex.getMessage(), ex);
          throw new SphinxException(ex.getMessage(), ex);
        }
      }
    }
  }

  private String[][] createSphinxData(SphinxSearch sphinxSearch) {
    List<Table> viewTables = new LinkedList<Table>();
    for (SphinxViewColumn columnParam : sphinxSearch.getViewColumns()) {
      Table t = new Table();
      t.setSchema(new Schema(columnParam.getReference().getSchema() == null ? sphinxSearch.getDefaultSchema() : columnParam.getReference().getSchema()));
      t.setName(columnParam.getReference().getTable());
      if (!viewTables.contains(t)) {
        viewTables.add(t);
      }
    }

    for (SphinxIndexingColumn columnParam : sphinxSearch.getIndexColumns()) {
      Table t = new Table();
      t.setSchema(new Schema(columnParam.getReference().getSchema() == null ? sphinxSearch.getDefaultSchema() : columnParam.getReference().getSchema()));
      t.setName(columnParam.getReference().getTable());
      if (!viewTables.contains(t)) {
        viewTables.add(t);
      }
    }

    String[] viewTableIds = new String[viewTables.size()];
    int index = 0;
    for (Table viewTable : viewTables) {
      StringBuilder builder = new StringBuilder();
      builder.append(viewTable.getSchema().getName()).append(".").append(viewTable.getName()).append(".").append(viewTable.getName()).append(Table.POSTFIX_TABLE_ID);
      viewTableIds[index++] = builder.toString();
    }

    String[] isIdArray = new String[viewTableIds.length + sphinxSearch.getViewColumns().size()];

    for (int i = 0; i < sphinxSearch.getViewColumns().size(); i++) {
      isIdArray[i] = "N";
    }
    for (int i = 0; i < viewTableIds.length; i++) {
      isIdArray[i + sphinxSearch.getViewColumns().size()] = "Y";
    }

    String[] viewColumns = new String[sphinxSearch.getViewColumns().size()];
    for (int j = 0; j < viewColumns.length; j++) {
      viewColumns[j] = sphinxSearch.getViewColumns().get(j).getReference().toString();
    }

    return new String[][] {viewColumns, viewTableIds, isIdArray};
  }

  @Override
  public void saveProjectStructure(Project project) {
    metadataDao.deleteProject(project.getProjectName());
    Long projectId = metadataDao.saveProject(project.getProjectName());

    saveProcesses(project.getProcesses(), projectId);

    for (Category category : project.getCategories()) {
      saveProjectCategory(category, projectId);
    }
  }

  private void saveProjectCategory(Category category, Long parentCategoryId) {
    Long categoryId = metadataDao.saveCategory(category.getCategoryName(), parentCategoryId);

    saveProcesses(category.getProcesses(), categoryId);

    for (Category childCategory : category.getCategories()) {
      saveProjectCategory(childCategory, categoryId);
    }
  }

  private void saveProcesses(List<Process> processes, Long categoryId) {
    if (processes.isEmpty()) {
      return;
    }

    List<String> processesNames = new ArrayList<String>(processes.size());

    for (Process process : processes) {
      processesNames.add(process.getProcessName());
    }

    metadataDao.saveProcesses(processesNames, categoryId);
  }

  @Override
  public void updateDbConnections(DeployedBusinessApplication application, String dataSourceJndiName, String driverClassName) {
    List<ProcessDbConnection> dbConnections = new ArrayList<ProcessDbConnection>(application.getProcesses().size());

    if (!application.getOldProcessDefinitionIds().isEmpty())
      metadataDao.deleteDbConnections(application.getOldProcessDefinitionIds());

    if (dataSourceJndiName == null)
      return;

    for (DeployedProcess process : application.getProcesses()) {
      dbConnections.add(new ProcessDbConnection(process.getDefinitionId(), dataSourceJndiName, driverClassName));
    }
    metadataDao.saveDbConnections(dbConnections);

    //todo transaction and exceptions
  }

  @Override
  public void saveProjectGroovyFunction(GroovyFunctionList groovyFunctionList, String projectName) {
    metadataDao.deleteProjectPredefinedGroovyFunctions(projectName);

    StringBuilder stringBuilder = new StringBuilder();

    for (GroovyFunction groovyFunction : groovyFunctionList.getGroovyFunctionList()) {
      String groovyFunctionCode = groovyFunction.getCode();
      if (groovyFunctionCode == null || groovyFunctionCode.isEmpty()) {
        continue;
      }

      String preparedGroovyScript = prepareGroovyScript(groovyFunctionCode);
      stringBuilder.append(preparedGroovyScript).append("\n");
    }

    String commonPredefinedFunctions;
    try {
      commonPredefinedFunctions = IOUtils.toString(getClass().getResourceAsStream("/groovy/common_predefined_functions.template"), "utf-8");
      stringBuilder.append(commonPredefinedFunctions);
    }
    catch (IOException ex) {
      logger.error(ex.toString(), ex);
    }

    metadataDao.insertProjectPredefinedGroovyFunctions(stringBuilder.toString(), projectName);

    groovyScriptExecutorCache.removeExecutor(projectName);
  }

  private String prepareGroovyScript(String groovyScript) {
    groovyScript = escapeSingleQuote(groovyScript);
    return escapeBrackets(groovyScript);
  }

  private String escapeBrackets(String string) {
    return string.replaceAll("\\{", "'\\{'").replaceAll("\\}", "'\\}'");
  }

  private String escapeSingleQuote(String string) {
    return string.replaceAll("\\'", "\\'\\'");
  }
}
