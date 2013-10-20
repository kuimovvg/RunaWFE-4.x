package ru.cg.runaex.database.service;

import java.util.List;

/**
 * @author Петров А.
 */
public interface UpdateDbService {

  void applyDb(String sql, List<String> schemas) throws Exception;

}
