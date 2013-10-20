package ru.cg.runaex.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.session.LinkTableInfo;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.LinkIds;
import ru.cg.runaex.database.bean.transport.SaveTransportData;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.dao.SaveDao;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.generatedb.bean.Table;
import ru.cg.runaex.web.security.SecurityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author urmancheev
 */
@Service
public class ActionButtonServiceImpl implements ActionButtonService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private BaseDao baseDao;
  @Autowired
  private SaveDao saveDao;
  @Autowired
  private RunaWfeService runaWfeService;

  @Override
  public HashMap<String, Object> save(List<Data> columnsData, Long processDefinitionId, Long processInstanceId) {
    //todo могут быть разные для разных полей
    String schema = columnsData.get(0).getSchema();
    String table = columnsData.get(0).getTable();

    Long rowId = getSelectedRow(schema, table, processInstanceId);

    SaveTransportData saveTransportData = new SaveTransportData(processInstanceId, rowId, schema, table, columnsData);
    rowId = saveDao.saveData(processDefinitionId, saveTransportData);

    ObjectInfo objectInfo = new ObjectInfo();
    objectInfo.setSchema(saveTransportData.getSchema());
    objectInfo.setTable(saveTransportData.getTable());
    objectInfo.setId(rowId);
    baseDao.saveOrUpdateObjectInfo(saveTransportData.getProcessInstanceId(), objectInfo);

    saveSelectedRow(rowId, schema, table, processInstanceId);
    HashMap<String, Object> changedRunaVariables = new HashMap<String, Object>(1);
    changedRunaVariables.put(WfeRunaVariables.SELECTED_ROW_ID_VARIABLE_NAME, rowId);

    return changedRunaVariables;
  }

  @Override
  public void delete(Long processDefinitionId, Long processInstanceId) {
    String strSelectedObjectInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO);
    ObjectInfo selectedObjectInfo = GsonUtil.getObjectFromJson(strSelectedObjectInfo, ObjectInfo.class);
    SaveTransportData deleteTransportData = new SaveTransportData(processInstanceId, selectedObjectInfo.getId(), selectedObjectInfo.getSchema(),
        selectedObjectInfo.getTable(), null);
    baseDao.deleteData(processDefinitionId, deleteTransportData);

    deleteSelectedRow(processInstanceId);
  }

  @Override
  public void link(Long processDefinitionId, Long processInstanceId) {
    String linkSchema;
    String linkTable;
    String baseObjectTable = null;
    Long baseObjectId = null;

    String strSelectedObjectInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO);
    ObjectInfo selectedObjectInfo = GsonUtil.getObjectFromJson(strSelectedObjectInfo, ObjectInfo.class);

    linkSchema = selectedObjectInfo.getSchema();
    linkTable = selectedObjectInfo.getTable();
    /**
     * get base object id
     */
    Map<Long, String> objectInfoMap = baseDao.getObjectInfoFromDb(processInstanceId);
    for (Long key : objectInfoMap.keySet()) {
      String jsonObjectInfo = objectInfoMap.get(key);
      logger.debug("jsonObjectInfo - " + jsonObjectInfo);
      ObjectInfo tmpObjectInfo = GsonUtil.getObjectFromJson(jsonObjectInfo, ObjectInfo.class);
      if (selectedObjectInfo.getBase().toString().equals(tmpObjectInfo.toString())) {
        baseObjectId = tmpObjectInfo.getId();
        baseObjectTable = tmpObjectInfo.getTable();
        break;
      }
    }

    List<Data> columnsData = new ArrayList<Data>(2);

    Data linkData = new Data();
    linkData.setField(baseObjectTable + Table.POSTFIX_TABLE_ID);
    linkData.setValue(baseObjectId);
    columnsData.add(linkData);

    linkData = new Data();
    linkData.setField(selectedObjectInfo.getAttachable().getTable() + Table.POSTFIX_TABLE_ID);
    linkData.setValue(selectedObjectInfo.getAttachable().getId());
    columnsData.add(linkData);

    SaveTransportData linkTransportData = new SaveTransportData(processInstanceId, null, linkSchema, linkTable, columnsData);
    baseDao.linkData(processDefinitionId, linkTransportData);
  }

  @Override
  public void unlink(Long processDefinitionId, Long processInstanceId) {
    Long baseObjectId = null;

    String strSelectedObjectInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO);
    ObjectInfo selectedObjectInfo = GsonUtil.getObjectFromJson(strSelectedObjectInfo, ObjectInfo.class);

    /**
     * get base object id
     */
    Map<Long, String> objectInfoMap = baseDao.getObjectInfoFromDb(processInstanceId);
    for (Long key : objectInfoMap.keySet()) {
      String jsonObjectInfo = objectInfoMap.get(key);
      logger.debug("jsonObjectInfo - " + jsonObjectInfo);
      ObjectInfo tmpObjectInfo = GsonUtil.getObjectFromJson(jsonObjectInfo, ObjectInfo.class);
      if (selectedObjectInfo.getBase().toString().equals(tmpObjectInfo.toString())) {
        baseObjectId = tmpObjectInfo.getId();
        break;
      }
    }

    List<Data> columnsData = new ArrayList<Data>(2);

    Data linkData = new Data();
    linkData.setField(selectedObjectInfo.getBase().getTable() + Table.POSTFIX_TABLE_ID);
    linkData.setValue(baseObjectId);
    columnsData.add(linkData);

    linkData = new Data();
    linkData.setField(selectedObjectInfo.getAttachable().getTable() + Table.POSTFIX_TABLE_ID);
    linkData.setValue(selectedObjectInfo.getAttachable().getId());
    columnsData.add(linkData);

    SaveTransportData deleteTransportData = new SaveTransportData(processInstanceId, null,
        selectedObjectInfo.getSchema(), selectedObjectInfo.getTable(), columnsData);
    baseDao.unlinkData(processDefinitionId, deleteTransportData);

  }

  @Override
  public HashMap<String, Object> saveAndLink(List<Data> columnsData, Long processDefinitionId, Long processInstanceId) {
    HashMap<String, Object> changedRunaVariables = new HashMap<String, Object>(1);

    //todo могут быть разные для разных полей
    String schema = columnsData.get(0).getSchema();
    String table = columnsData.get(0).getTable();

    Long selectedRowId = getSelectedRow(schema, table, processInstanceId);
    SaveTransportData saveTransportData = new SaveTransportData(processInstanceId, selectedRowId, schema, table, columnsData);

    String jsonLinkTableInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.LINK_TABLE_INFO);
    LinkTableInfo linkTableInfo = GsonUtil.getObjectFromJson(jsonLinkTableInfo, LinkTableInfo.class);

    String baseObjectScheme = linkTableInfo.getBaseObjectSchema();
    String baseObjectTable = linkTableInfo.getBaseObjectTable();

    boolean isDependentTable = schema.equals(linkTableInfo.getSchema()) && table.equals(linkTableInfo.getTable());
    Map<Long, String> objectMap = baseDao.getObjectInfoFromDb(processInstanceId);

    if (!isDependentTable) {
      List<Data> linkData = new ArrayList<Data>(2);
      for (String jsonObjectInfo : objectMap.values()) {
        ObjectInfo objectInfo = GsonUtil.getObjectFromJson(jsonObjectInfo, ObjectInfo.class);
        if (objectInfo.getTable().equals(baseObjectTable) && objectInfo.getSchema().equals(baseObjectScheme)) {
          Data baseObjectData = new Data(baseObjectTable.concat(Table.POSTFIX_TABLE_ID), objectInfo.getId(), "Long");
          linkData.add(baseObjectData);
        }
      }
      SaveTransportData linkTransportData = new SaveTransportData(processInstanceId, null, linkTableInfo.getSchema(), linkTableInfo.getTable(), linkData);

      LinkIds linkIds = saveDao.saveAndLinkData(processDefinitionId, saveTransportData, linkTransportData);

      ObjectInfo objectInfo = new ObjectInfo();
      objectInfo.setSchema(saveTransportData.getSchema());
      objectInfo.setTable(saveTransportData.getTable());
      objectInfo.setId(linkIds.getLinkedObjectId());
      baseDao.saveOrUpdateObjectInfo(saveTransportData.getProcessInstanceId(), objectInfo);

      objectInfo = new ObjectInfo();
      objectInfo.setSchema(linkTransportData.getSchema());
      objectInfo.setTable(linkTransportData.getTable());
      objectInfo.setId(linkIds.getLinkId());
      ObjectInfo base = new ObjectInfo();
      base.setSchema("");
      base.setTable("");
      base.setId(null);
      objectInfo.setBase(base);
      ObjectInfo attachable = new ObjectInfo();
      attachable.setSchema("");
      attachable.setTable("");
      attachable.setId(null);
      objectInfo.setAttachable(attachable);
      baseDao.saveOrUpdateObjectInfo(linkTransportData.getProcessInstanceId(), objectInfo);
    }
    else {
      for (String jsonObjectInfo : objectMap.values()) {
        ObjectInfo objectInfo = GsonUtil.getObjectFromJson(jsonObjectInfo, ObjectInfo.class);
        if (objectInfo.getTable().equals(baseObjectTable) && objectInfo.getSchema().equals(baseObjectScheme)) {
          Data baseObjectData = new Data(baseObjectTable.concat(Table.POSTFIX_TABLE_ID), objectInfo.getId(), "Long");
          saveTransportData.getData().add(baseObjectData);
          break;
        }
      }
      Long rowId = saveDao.saveData(processDefinitionId, saveTransportData);

      ObjectInfo selectedObjectInfo = new ObjectInfo();
      selectedObjectInfo.setId(selectedRowId);
      selectedObjectInfo.setSchema(schema);
      selectedObjectInfo.setTable(table);
      baseDao.saveOrUpdateObjectInfo(processInstanceId, selectedObjectInfo);

      saveSelectedRow(rowId, schema, table, processInstanceId);
      changedRunaVariables.put(WfeRunaVariables.SELECTED_ROW_ID_VARIABLE_NAME, rowId);
    }

    return changedRunaVariables;
  }

  @Override
  public void find(Map<String, List<Data>> filterDataByTableId, Long processInstanceId) {
    for (Map.Entry<String, List<Data>> entry : filterDataByTableId.entrySet()) {
      String tableId = entry.getKey();
      List<Data> columnsData = entry.getValue();

      String filterKey = WfeRunaVariables.getFilterKeyVariable(tableId);
      String jsonData = GsonUtil.toJson(new TransportData(0, columnsData));
      baseDao.addVariableToDb(processInstanceId, filterKey, jsonData);  //todo batch update
    }
  }

  private Long getSelectedRow(String schema, String table, Long processInstanceId) {
    Long selectedRowId = null;
    Map<Long, String> objectInfoMap = baseDao.getObjectInfoFromDb(processInstanceId);
    for (Long key : objectInfoMap.keySet()) {
      ObjectInfo tmpObjectInfo = GsonUtil.getObjectFromJson(objectInfoMap.get(key), ObjectInfo.class);
      if ((schema + "." + table).equals(tmpObjectInfo.toString())) {
        selectedRowId = tmpObjectInfo.getId();
        break;
      }
    }
    return selectedRowId;
  }

  private void saveSelectedRow(Long selectedRowId, String schema, String table, Long processInstanceId) {
    ObjectInfo selectedObjectInfo = new ObjectInfo();
    selectedObjectInfo.setId(selectedRowId);
    selectedObjectInfo.setSchema(schema);
    selectedObjectInfo.setTable(table);

    baseDao.addVariableToDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO, GsonUtil.toJson(selectedObjectInfo));
  }

  private void deleteSelectedRow(Long processInstanceId) {
    baseDao.removeVariableFromDb(WfeRunaVariables.SELECTED_OBJECT_INFO, processInstanceId);
    runaWfeService.removeVariable(SecurityUtils.getCurrentRunaUser(), processInstanceId, WfeRunaVariables.SELECTED_ROW_ID_VARIABLE_NAME);
  }
}
