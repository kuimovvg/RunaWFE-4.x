package ru.cg.runaex.generate_security.dao;

import ru.cg.runaex.generate_security.model.Module;

/**
 * @author urmancheev
 */
public interface ModuleDao {

  Long getIdByName(String name);

  Long save(Module module);

  void delete(Long moduleId);
}
