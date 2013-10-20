package ru.cg.runaex.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.cg.runaex.database.dao.BaseDao;

/**
 * @author Абдулин Ильдар
 */
@Service
public class ContextVariableServiceImpl implements ContextVariableService {

  @Autowired
  private BaseDao baseDao;

  @Override
  public void removeVariableFromDb(Long processInstanceId) {
    baseDao.removeVariableFromDb(processInstanceId);
  }
}
