package ru.cg.runaex.database.dao;

/**
 * Date: 16.08.12
 * Time: 11:01
 *
 * @author Sabirov
 */
public class ComponentDbServices {
  private BaseDao baseDaoService;

  public ComponentDbServices(BaseDao baseDaoService) {
    this.baseDaoService = baseDaoService;
  }

  public BaseDao getBaseDaoService() {
    return baseDaoService;
  }
}
