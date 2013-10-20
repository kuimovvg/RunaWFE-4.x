package ru.cg.runaex.database.dao;

import org.springframework.dao.DataAccessException;

import ru.cg.runaex.database.bean.transport.LinkIds;
import ru.cg.runaex.database.bean.transport.SaveTransportData;

/**
 * @author urmancheev
 */
public interface SaveDao {
  /**
   * Save data.
   * Need fill bean SaveTransportData. Not need set the bean Data.classType
   *
   * @param saveTransportData - data
   * @return id if create object data
   * @throws org.springframework.dao.DataAccessException
   *
   */
  Long saveData(Long processDefinitionId, SaveTransportData saveTransportData) throws DataAccessException;

  LinkIds saveAndLinkData(Long processDefinitionId, SaveTransportData saveTransportData, SaveTransportData linkTransportData) throws DataAccessException;
}
