package ru.cg.runaex.runa_ext.handler.dao;

import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import ru.runa.wfe.InternalApplicationException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.SaveTransportData;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.dao.BaseDao;

/**
 * @author urmancheev
 */
public class TaskHandlerDatabaseDaoHelper {
  private static final Logger logger = LoggerFactory.getLogger(TaskHandlerDatabaseDaoHelper.class);

  public static void saveResultsToDatabase(Long processInstanceId, Long processDefinitionId, List<Data> dataList) throws Exception {
    BaseDao baseDao = DatabaseSpringContext.getBaseDao();
    ObjectInfo selectedObjectInfo = loadSelectedObjectInfo(processInstanceId);

    String schema = selectedObjectInfo.getSchema();
    String table = selectedObjectInfo.getTable();

    SaveTransportData saveTransportData = new SaveTransportData(processInstanceId, selectedObjectInfo.getId(), schema, table, dataList);
    try {
      Long rowId = DatabaseSpringContext.getSaveDao().saveData(processDefinitionId, saveTransportData);

      selectedObjectInfo = new ObjectInfo();
      selectedObjectInfo.setId(rowId);
      selectedObjectInfo.setSchema(schema);
      selectedObjectInfo.setTable(table);
      baseDao.saveOrUpdateObjectInfo(processInstanceId, selectedObjectInfo);
    }
    catch (DataAccessException ex) {
      logger.error("Could not save results to database.", ex);
      throw new InternalApplicationException("Could not save results to database.", ex);
    }
  }

  protected static ObjectInfo loadSelectedObjectInfo(Long processInstanceId) throws Exception {
    BaseDao baseDao = DatabaseSpringContext.getBaseDao();

    String strSelectedObjectInfo;
    try {
      strSelectedObjectInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO);
    }
    catch (DataAccessException ex) {
      logger.error("Could not load selected object info.", ex);
      throw new InternalApplicationException("Could not load selected object info.", ex);
    }

    return new Gson().fromJson(strSelectedObjectInfo, ObjectInfo.class);
  }

  public static TransportData loadData(Long processInstanceId, Long processDefinitionId, Collection<String> columns) throws Exception {
    ObjectInfo selectedObjectInfo = loadSelectedObjectInfo(processInstanceId);

    BaseDao baseDao = DatabaseSpringContext.getBaseDao();
    return baseDao.getDataById(processDefinitionId, selectedObjectInfo.getSchema(), selectedObjectInfo.getTable(), columns, selectedObjectInfo.getId());
  }
}
