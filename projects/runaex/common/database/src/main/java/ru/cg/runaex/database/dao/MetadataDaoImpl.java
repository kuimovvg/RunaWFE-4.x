package ru.cg.runaex.database.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import ru.cg.runaex.database.bean.DependencyMatrix;
import ru.cg.runaex.database.bean.model.Category;
import ru.cg.runaex.database.bean.model.FiasColumn;
import ru.cg.runaex.database.bean.model.MetadataEditableTreeGrid;
import ru.cg.runaex.database.bean.model.ProcessDbConnection;
import ru.cg.runaex.database.dao.util.JdbcTemplateProvider;
import ru.cg.runaex.database.dao.util.QueryHelper;
import ru.cg.runaex.database.dao.util.ResultSetHelper;

import java.sql.*;
import java.util.*;

/**
 * @author Петров А.
 */
public class MetadataDaoImpl implements MetadataDao {

  private static final String DELETE_PROJECT_QUERY = "delete from metadata.category where parent_category_id is null and category_name = ?";
  private static final String INSERT_CATEGORY_QUERY = "insert into metadata.category (parent_category_id, category_name) values (?, ?)";
  private static final String INSERT_PROCESSES_QUERY = "insert into metadata.link_process_category(category_id, process_name) values (?, ?)";
  private static final String LOAD_ALL_PROJECTS_QUERY = "select category_id, parent_category_id, category_name from metadata.category";
  private static final String LOAD_ALL_PROCESSES_QUERY = "select category_id, process_name from metadata.link_process_category";
  private static final String INSERT_GROOVY_SCRIPT_QUERY = "insert into metadata.groovy_scripts (script) values (?)";
  private static final String SELECT_GROOVY_SCRIPT_BY_ID_QUERY = "select script from metadata.groovy_scripts where script_id = ?";
  private static final String DELETE_GROOVY_SCRIPT_QUERY = "delete from metadata.groovy_scripts where creation_date <= now() - interval ?";

  private static final String INSERT_PROJECT_GROOVY_FUNCTIONS = "insert into metadata.project_groovy_functions (code, project_name) values (?, ?)";
  private static final String DELETE_PROJECT_GROOVY_FUNCTIONS = "delete from metadata.project_groovy_functions where project_name = ?";
  private static final String SELECT_PROJECT_GROOVY_FUNCTIONS = "select code from metadata.project_groovy_functions where project_name = ?";

  private JdbcTemplate jdbcTemplate;
  private JdbcTemplateProvider templateProvider;

  public MetadataDaoImpl(JdbcTemplate template) {
    this.jdbcTemplate = template;
  }

  public void setTemplateProvider(JdbcTemplateProvider templateProvider) {
    this.templateProvider = templateProvider;
  }

  @Override
  public void deleteProject(String categoryName) {
    jdbcTemplate.update(DELETE_PROJECT_QUERY, categoryName);
  }

  @Override
  public Long saveProject(final String projectName) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(INSERT_CATEGORY_QUERY, new String[] {"category_id"});

        ps.setNull(1, Types.BIGINT);
        ps.setString(2, projectName);

        return ps;
      }
    }, keyHolder);

    return keyHolder.getKey().longValue();
  }

  @Override
  public Long saveGroovyScript(final String script) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(INSERT_GROOVY_SCRIPT_QUERY, new String[] {"script_id"});
        ps.setString(1, script);

        return ps;
      }
    }, keyHolder);
    return keyHolder.getKey().longValue();
  }

  @Override
  public String getGroovyScript(Long id) {
    return jdbcTemplate.queryForObject(SELECT_GROOVY_SCRIPT_BY_ID_QUERY, String.class, id);
  }

  @Override
  public Long saveCategory(final String categoryName, final Long parentCategoryId) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(INSERT_CATEGORY_QUERY, new String[] {"category_id"});
        ps.setLong(1, parentCategoryId);
        ps.setString(2, categoryName);

        return ps;
      }
    }, keyHolder);

    return keyHolder.getKey().longValue();
  }

  @Override
  public void saveProcesses(List<String> processesNames, final Long categoryId) {
    final String[] processesNamesArray = processesNames.toArray(new String[processesNames.size()]);
    jdbcTemplate.batchUpdate(INSERT_PROCESSES_QUERY, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setLong(1, categoryId);
        ps.setString(2, processesNamesArray[i]);
      }

      @Override
      public int getBatchSize() {
        return processesNamesArray.length;
      }
    });
  }

  @Override
  public List<Category> loadAllProjects() {
    return jdbcTemplate.query(LOAD_ALL_PROJECTS_QUERY, new ResultSetExtractor<List<Category>>() {
      @Override
      public List<Category> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Category> result = new LinkedList<Category>();

        Long categoryId;
        Long parentCategoryId;
        String categoryName;

        while (rs.next()) {
          categoryId = rs.getLong("category_id");
          parentCategoryId = rs.getLong("parent_category_id");
          categoryName = rs.getString("category_name");

          Category category = new Category();
          category.setId(categoryId);
          category.setParentId(parentCategoryId);
          category.setName(categoryName);

          result.add(category);
        }

        return result;
      }
    });
  }

  @Override
  public List<Category> loadProjectsWithInboxFilter(Set<Long> categoriesByTasks) {
    return jdbcTemplate.query("select category_id, parent_category_id, category_name from metadata.category where category_id in (" + QueryHelper.preparePlaceHolders(categoriesByTasks.size()) + ")",
        categoriesByTasks.toArray(), new ResultSetExtractor<List<Category>>() {
      @Override
      public List<Category> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Category> result = new LinkedList<Category>();

        Long categoryId;
        Long parentCategoryId;
        String categoryName;

        while (rs.next()) {
          categoryId = rs.getLong("category_id");
          parentCategoryId = rs.getLong("parent_category_id");
          categoryName = rs.getString("category_name");

          Category category = new Category();
          category.setId(categoryId);
          category.setParentId(parentCategoryId);
          category.setName(categoryName);

          result.add(category);
        }

        return result;
      }
    });
  }

  @Override
  public Map<String, Long> loadAllProcessesCategoriesLinks() {
    return jdbcTemplate.query(LOAD_ALL_PROCESSES_QUERY, new ResultSetExtractor<Map<String, Long>>() {
      @Override
      public Map<String, Long> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, Long> result = new HashMap<String, Long>();

        Long categoryId;
        String processName;

        while (rs.next()) {
          categoryId = rs.getLong("category_id");
          processName = rs.getString("process_name");

          result.put(processName, categoryId);
        }

        return result;
      }
    });
  }

  @Override
  public Map<String, Long> loadLinksWithInboxFilter(Set<String> businessProcessesByTasks) {
    return jdbcTemplate.query("select category_id, process_name from metadata.link_process_category where process_name in (" + QueryHelper.preparePlaceHolders(businessProcessesByTasks.size()) + ")",
        businessProcessesByTasks.toArray(),
        new ResultSetExtractor<Map<String, Long>>() {
          @Override
          public Map<String, Long> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<String, Long> result = new HashMap<String, Long>();

            Long categoryId;
            String processName;

            while (rs.next()) {
              categoryId = rs.getLong("category_id");
              processName = rs.getString("process_name");

              result.put(processName, categoryId);
            }

            return result;
          }
        });
  }

  @Override
  public void deleteDbConnections(List<Long> deletedProcesses) {
    jdbcTemplate.update("delete from metadata.db_connection where process_definition_id in ("
        + QueryHelper.preparePlaceHolders(deletedProcesses.size()) + ")", deletedProcesses.toArray());
  }

  @Override
  public void saveDbConnections(List<ProcessDbConnection> dbConnections) {
    List<Object[]> args = new ArrayList<Object[]>(dbConnections.size());
    for (ProcessDbConnection dbConnection : dbConnections)
      args.add(new Object[] {dbConnection.getProcessDefinitionId(), dbConnection.getJndiName(), dbConnection.getDriverClassName()});
    int[] argTypes = {Types.BIGINT, Types.VARCHAR, Types.VARCHAR};

    jdbcTemplate.batchUpdate("insert into metadata.db_connection (process_definition_id, jndi_name, driver_class_name) values (?, ?, ?)", args, argTypes);
  }

  @Override
  public String getDbConnectionJndiName(Long processDefinitionId) {
    return jdbcTemplate.query("select jndi_name from metadata.db_connection where process_definition_id = ?", new Long[] {processDefinitionId}, new ResultSetExtractor<String>() {
      @Override
      public String extractData(ResultSet rs) throws SQLException, DataAccessException {
        if (rs.next())
          return rs.getString(1);
        else
          return null;
      }
    });
  }

  @Override
  public List<Long> getAllProcessDefinitionsByDbConnections() {
    return jdbcTemplate.query("select process_definition_id from metadata.db_connection", new ResultSetExtractor<List<Long>>() {
      @Override
      public List<Long> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Long> idList = new ArrayList<Long>();
        while (rs.next())
          idList.add(rs.getLong(1));

        return idList;
      }
    });
  }

  @Override
  public void deleteExpiredGroovyScripts(String expiryDate) {
    String query = DELETE_GROOVY_SCRIPT_QUERY.replace("?", "'" + expiryDate + "'");
    jdbcTemplate.update(query);
  }

  @Override
  public void insertProjectPredefinedGroovyFunctions(String code, String projectName) {
    jdbcTemplate.update(INSERT_PROJECT_GROOVY_FUNCTIONS, new Object[] {code, projectName}, new int[] {Types.VARCHAR, Types.VARCHAR});
  }

  @Override
  public void deleteProjectPredefinedGroovyFunctions(String projectName) {
    jdbcTemplate.update(DELETE_PROJECT_GROOVY_FUNCTIONS, new Object[] {projectName}, new int[] {Types.VARCHAR});
  }

  @Override
  public String loadProjectPredefinedGroovyFunctions(String projectName) {
    return jdbcTemplate.query(SELECT_PROJECT_GROOVY_FUNCTIONS, new Object[] {projectName}, new int[] {Types.VARCHAR}, new ResultSetExtractor<String>() {
      @Override
      public String extractData(ResultSet rs) throws SQLException, DataAccessException {
        if (rs.next()) {
          return rs.getString(1);
        }

        return null;
      }
    });
  }

  @Override
  public void deleteFiasColumns(List<Long> deletedProcesses) {
    jdbcTemplate.update("delete from metadata.fias_columns where source_process_definition_id in ("
        + QueryHelper.preparePlaceHolders(deletedProcesses.size()) + ")", deletedProcesses.toArray());
  }

  @Override
  public void saveFiasColumns(final List<FiasColumn> fiasColumns) {
    for (final FiasColumn fiasColumn : fiasColumns) {
      jdbcTemplate.update(new PreparedStatementCreator() {
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
          PreparedStatement ps = con.prepareStatement("insert into metadata.fias_columns (" +
              "source_process_definition_id, data_source_jndi_name, table_name, fields) values (?, ?, ?, ?)");

          ps.setLong(1, fiasColumn.getProcessId());
          ps.setString(2, fiasColumn.getDataSourceJndiName());
          ps.setString(3, fiasColumn.getTable());
          ps.setArray(4, con.createArrayOf("varchar", fiasColumn.getFields()));

          return ps;
        }
      });
    }
  }

  @Override
  public Set<String> getFiasColumns(Long currentProcessDefinitionId, String schemaName, String tableName) {
    String tableWithSchema = schemaName.concat(".").concat(tableName);
    Object[] args = new Object[] {tableWithSchema, currentProcessDefinitionId};
    int[] types = new int[] {Types.VARCHAR, Types.BIGINT};
    List<String[]> results = jdbcTemplate.query("select fc.fields from metadata.fias_columns fc " +
        "where fc.table_name = ? and (fc.data_source_jndi_name is null or fc.data_source_jndi_name = " +
        "(select dc.jndi_name from metadata.db_connection dc where dc.process_definition_id = ?))", args, types, new RowMapper<String[]>() {
      @Override
      public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (String[]) ResultSetHelper.getValue(rs, 1);
      }
    });
    Set<String> fields = new HashSet<String>();
    for (String[] result : results) {
      Collections.addAll(fields, result);
    }
    return fields;
  }

  @Override
  public ProcessDbConnection getProjectDatabaseConnectionInfo(final Long processDefinitionId) {
    return jdbcTemplate.query("select jndi_name, driver_class_name from metadata.db_connection where process_definition_id = ?", new Object[] {processDefinitionId}, new int[] {Types.BIGINT}, new ResultSetExtractor<ProcessDbConnection>() {
      @Override
      public ProcessDbConnection extractData(ResultSet rs) throws SQLException, DataAccessException {
        String jndiName = null;
        String driverClassName = null;
        if (rs.next()) {
          jndiName = rs.getString(1);
          driverClassName = rs.getString(2);
        }

        return new ProcessDbConnection(processDefinitionId, jndiName, driverClassName);
      }
    });
  }

  @Override
  public MetadataEditableTreeGrid getEditableTreeGrid(final String tableId) {
    return jdbcTemplate.query("SELECT * FROM metadata.editable_tree_grid WHERE table_id=?", new Object[] {tableId}, new ResultSetExtractor<MetadataEditableTreeGrid>() {
      @Override
      public MetadataEditableTreeGrid extractData(ResultSet rs) throws SQLException, DataAccessException {

        while (rs.next()) {
          DependencyMatrix dependencyMatrix = DependencyMatrix.fromJson(rs.getString("dependency_matrix"));
          return new MetadataEditableTreeGrid(rs.getLong("editable_tree_grid_id"), tableId, rs.getString("business_rule"), rs.getString("css_class"), rs.getString("editable_rule"), dependencyMatrix);
        }
        return null;
      }
    });
  }

  @Override
  public void saveOrUpdateEditableTreeGrid(MetadataEditableTreeGrid editableTreeGrid) {
    String dependencyMatrix = "";
    if (editableTreeGrid.getDependencyMatrix() != null) {
      dependencyMatrix = editableTreeGrid.getDependencyMatrix().toJson();
    }
    if (editableTreeGrid.getId() == null) {
      jdbcTemplate.update("INSERT INTO metadata.editable_tree_grid (table_id,business_rule,css_class,editable_rule,dependency_matrix) VALUES (?,?,?,?,?)", new Object[] {editableTreeGrid.getTableId(), editableTreeGrid.getBusinessRule(), editableTreeGrid.getCssClass(), editableTreeGrid.getEditableRule(), dependencyMatrix});
    }
    else
      jdbcTemplate.update("UPDATE metadata.editable_tree_grid SET table_id=?, business_rule=?, css_class=?, editable_rule=?, dependency_matrix=? WHERE editable_tree_grid_id=?", new Object[] {editableTreeGrid.getTableId(), editableTreeGrid.getBusinessRule(), editableTreeGrid.getCssClass(), editableTreeGrid.getEditableRule(), dependencyMatrix, editableTreeGrid.getId()});
  }

  public void deleteSphinxData(final List<Long> ids) throws DataAccessException {
    jdbcTemplate.update("delete from metadata.sphinx_search where sphinx_search_id in ("
        + QueryHelper.preparePlaceHolders(ids.size()) + ")", ids.toArray());
  }
}
