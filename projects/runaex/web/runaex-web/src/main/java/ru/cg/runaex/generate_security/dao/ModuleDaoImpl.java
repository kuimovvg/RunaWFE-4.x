package ru.cg.runaex.generate_security.dao;

import java.sql.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.cg.runaex.generate_security.model.Module;

/**
 * @author urmancheev
 */
@Repository
public class ModuleDaoImpl implements ModuleDao {
  @Autowired
  @Qualifier("securityManagerJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  @Override
  public Long getIdByName(String name) {
    String sql = "select module_id from module where name = ?";
    return jdbcTemplate.query(sql, new String[] {name}, new ResultSetExtractor<Long>() {
      @Override
      public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
        if (!rs.next())
          return null;
        return rs.getLong("module_id");
      }
    });
  }

  @Override
  public Long save(final Module module) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("insert into module (name) values (?)", new String[] {"module_id"});
        ps.setString(1, module.getName());
        return ps;
      }
    }, keyHolder);
    return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
  }

  @Override
  public void delete(Long moduleId) {
    jdbcTemplate.update("delete from module where module_id = ?", new Object[] {moduleId}, new int[] {Types.BIGINT});
  }
}
