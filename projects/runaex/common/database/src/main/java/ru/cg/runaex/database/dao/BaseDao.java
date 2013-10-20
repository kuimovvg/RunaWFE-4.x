package ru.cg.runaex.database.dao;


import org.springframework.dao.DataAccessException;
import ru.cg.runaex.database.bean.transport.SaveTransportData;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.components.bean.session.ObjectInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author donskoy
 */
public interface BaseDao {
  /**
   * Get data
   * <p/>
   * if object1 and object2 is not empty then need to get data from link table from to column-reference table(object1 and object2)
   *
   * @param schema              - schema name
   * @param table               - table name
   * @param fields              - fields
   * @param fromItems           - from items
   * @param limitItems          - limit items
   * @param sortName            - sort field name
   * @param sortOrder           - sort order
   * @param query               - query (regex)
   * @param queryField          - query by field name
   * @param filterTransportData - filter transport data
   * @return data
   * @throws DataAccessException
   */
  public TransportDataSet getData(Long processDefinitionId, final String schema, final String table, final String[] fields,
                                  final Integer fromItems, final Integer limitItems,
                                  final String sortName, final String sortOrder,
                                  final String query, final String queryField,
                                  final TransportData filterTransportData,
                                  final String object1, final String object2, final String editTreeLinkColumns) throws DataAccessException;

  /**
   * Get data by id
   *
   * @param id         - id
   * @param schema     - schema name
   * @param table      - table name
   * @param field      - field
   * @param references - references table
   * @return data by id
   * @throws DataAccessException
   */
  TransportData getDataById(Long processDefinitionId, final Long id, final String schema, final String table, final String field, final String references) throws DataAccessException;

  /**
   * Update value from editable tree grid.
   *
   * @throws DataAccessException
   */
  Long updateTreeCell(final String col, final Long rowId, final Object value, Long processDefinitionId, TransportData filter) throws DataAccessException;


  /**
   * Delete data.
   * Need fill bean SaveTransportData. Not need set the bean Data.classType
   *
   * @param transportData - data
   * @return count affected rows
   * @throws DataAccessException
   */
  Integer deleteData(Long processDefinitionId, final SaveTransportData transportData) throws DataAccessException;

  /**
   * Save link data.
   * Need fill bean SaveTransportData. Not need set the bean Data.classType
   *
   * @param saveTransportData - data
   * @return id if create object data
   * @throws DataAccessException
   */
  Long linkData(Long processDefinitionId, final SaveTransportData saveTransportData) throws DataAccessException;

  /**
   * Unlink data.
   * Need fill bean SaveTransportData. Not need set the bean Data.classType
   *
   * @param transportData - data
   * @return count affected rows
   * @throws DataAccessException
   */
  Integer unlinkData(Long processDefinitionId, final SaveTransportData transportData) throws DataAccessException;

  /**
   * Add variable
   *
   * @param variableName - variable id
   * @param jsonData     - json data
   * @throws DataAccessException
   */
  void addVariableToDb(final Long processInstanceId, final String variableName, final String jsonData) throws DataAccessException;

  /**
   * Remove variable
   *
   * @param processInstanceId - variable id
   * @throws DataAccessException
   */
  void removeVariableFromDb(final Long processInstanceId) throws DataAccessException;


  /**
   * @param variableName
   * @param processInstanceId
   */
  void removeVariableFromDb(final String variableName, final Long processInstanceId);

  /**
   * Get variable
   *
   * @param variableName - variable id
   * @return json data
   * @throws DataAccessException
   */
  String getVariableFromDb(final Long processInstanceId, final String variableName) throws DataAccessException;

  /**
   * Get list object info
   *
   * @param processInstanceId - process instance id
   * @return map object infzo
   * @throws DataAccessException
   */
  Map<Long, String> getObjectInfoFromDb(final Long processInstanceId) throws DataAccessException;

  /**
   * Save or update objectInfo
   *
   * @param processInstanceId - process instance id
   * @param objectInfo        - objectInfo
   * @throws DataAccessException
   */
  void saveOrUpdateObjectInfo(final Long processInstanceId, final ObjectInfo objectInfo) throws DataAccessException;

  /**
   * Add objectInfo
   *
   * @param processInstanceId - process instance id
   * @param objectInfo        - objectInfo
   * @throws DataAccessException
   */
  boolean addObjectInfo(final Long processInstanceId, final ObjectInfo objectInfo) throws DataAccessException;

  /**
   * Chg map objectInfo
   *
   * @param map - map Map<Long, String>
   * @throws DataAccessException
   */
  Integer chgObjectInfo(Map<Long, String> map) throws DataAccessException;

  /**
   * Get data by field value
   *
   * @param schema - schema name
   * @param table  - table name
   * @param field  - field
   * @param value  - field value
   * @return first query result
   * @throws DataAccessException
   */
  TransportData getIdByFieldValue(Long processDefinitionId, final String schema, final String table, final String field, final String value) throws DataAccessException;

//
//  /**
//   * Remove objectInfo
//   * @param pId - primary id
//   * @throws DataAccessException
//   */
//  Integer removeObjectInfo(final Long pId) throws DataAccessException;

  List<String> getColumns(Long processDefinitionId, final String schema, final String table);

  TransportData getDataById(Long processDefinitionId, String schema, String table, String[] columns, Long id);

  TransportData getDataById(Long processDefinitionId, String schema, String table, Collection<String> columns, Long id) throws DataAccessException;

  Long getSequenceNextValue(Long processDefinitionId, String schemaName, String sequenceName);


}