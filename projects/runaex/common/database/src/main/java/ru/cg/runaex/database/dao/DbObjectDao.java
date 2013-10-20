package ru.cg.runaex.database.dao;

import java.util.List;

/**
 * @author Петров А.
 */
public interface DbObjectDao {

  void executeSqlOnTmpDb(String sql);

  void createSchema(String schemaName);

  List<String> getSchemas();
}
