package ru.cg.runaex.generate_security.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.cg.runaex.generate_security.model.Function;

/**
 * @author urmancheev
 */
@Repository
public class FunctionDaoImpl implements FunctionDao {

  @Autowired
  @Qualifier("securityManagerJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  @Override
  public Long save(final Function function) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("insert into function (module_id, name, url, ignore_url_ending, request_type) values (?, ?, ?, ?, ?)",
            new String[] {"function_id"});
        ps.setLong(1, function.getModuleId());
        ps.setString(2, function.getName());
        ps.setString(3, function.getUrl());
        ps.setBoolean(4, function.getIgnoreUrlEnding());
        ps.setString(5, function.getRequestType().name());
        return ps;
      }
    }, keyHolder);
    return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
  }

  @Override
  public void saveParameters(Long functionId, List<Function.Parameter> parameters) {
    List<Object[]> args = new ArrayList<Object[]>(parameters.size());
    for (Function.Parameter parameter : parameters)
      args.add(new Object[] {functionId, parameter.getName(), parameter.getValue()});
    int[] argTypes = {Types.BIGINT, Types.VARCHAR, Types.VARCHAR};

    jdbcTemplate.batchUpdate("insert into function_parameter (function_id, name, value) values (?, ?, ?)", args, argTypes);
  }
}
