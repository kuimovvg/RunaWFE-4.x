package ru.cg.runaex.database.dao.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Date: 16.08.12
 * Time: 15:25
 *
 * @author Sabirov
 */
public class CustomMapper implements RowMapper<DbBean> {
  public DbBean mapRow(ResultSet rs, int rowNum) throws SQLException {
    DbBean dbBean = new DbBean();
    dbBean.setName(rs.getString("name"));
    return dbBean;
  }
}