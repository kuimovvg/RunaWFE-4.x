package ru.cg.runaex.database.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import ru.cg.runaex.database.util.Closeable;

/**
 * @author Петров А.
 */
public class DbObjectDaoImpl implements DbObjectDao {

  private Log logger = LogFactory.getLog(getClass());

  private JdbcTemplate jdbcTemplate;
  private JdbcTemplate runaexJdbcTemplate;
  private JdbcTemplate runaexTmpJdbcTemplate;

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public JdbcTemplate getRunaexJdbcTemplate() {
    return runaexJdbcTemplate;
  }

  public void setRunaexJdbcTemplate(JdbcTemplate runaexJdbcTemplate) {
    this.runaexJdbcTemplate = runaexJdbcTemplate;
  }

  public JdbcTemplate getRunaexTmpJdbcTemplate() {
    return runaexTmpJdbcTemplate;
  }

  public void setRunaexTmpJdbcTemplate(JdbcTemplate runaexTmpJdbcTemplate) {
    this.runaexTmpJdbcTemplate = runaexTmpJdbcTemplate;
  }

  @Override
  public void executeSqlOnTmpDb(String sql) {
    runaexTmpJdbcTemplate.execute(sql);
  }

  @Override
  public void createSchema(final String schemaName) {
    runaexJdbcTemplate.execute(new ConnectionCallback<Object>() {
      @Override
      public Object doInConnection(Connection con) throws SQLException, DataAccessException {
        Statement st = null;
        try {
          st = con.createStatement();
          st.execute("CREATE SCHEMA " + schemaName + ";");
        }
        catch (SQLException ex) {
          logger.error(ex.toString(), ex);
          throw ex;
        }
        catch (DataAccessException ex) {
          logger.error(ex.toString(), ex);
          throw ex;
        }
        finally {
          Closeable.close(st);
        }
        return null;
      }
    });
  }

  @Override
  public List<String> getSchemas() {
    return runaexJdbcTemplate.execute(new ConnectionCallback<List<String>>() {
      @Override
      public List<String> doInConnection(Connection con) throws SQLException, DataAccessException {
        Statement st = null;
        ResultSet rs = null;
        try {
          st = con.createStatement();
          rs = st.executeQuery("select schema_name\n" +
              "from information_schema.schemata\n" +
              " -- and to exclude 'system' schemata:\n" +
              "where schema_name <> 'information_schema'\n" +
              "  and schema_name !~ E'^pg_'");
          List<String> schemas = new ArrayList<String>();
          while (rs.next()) {
            schemas.add(rs.getString("schema_name"));
          }
          return schemas;
        }
        catch (SQLException ex) {
          logger.error(ex.toString(), ex);
          throw ex;
        }
        catch (DataAccessException ex) {
          logger.error(ex.toString(), ex);
          throw ex;
        }
        finally {
          Closeable.close(rs);
          Closeable.close(st);
        }
      }
    });
  }
}
