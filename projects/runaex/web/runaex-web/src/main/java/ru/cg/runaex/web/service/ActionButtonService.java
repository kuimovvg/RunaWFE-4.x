package ru.cg.runaex.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.cg.runaex.database.bean.transport.Data;


/**
 * @author urmancheev
 */
public interface ActionButtonService {
  HashMap<String, Object> save(List<Data> columnsData, Long processDefinitionId, Long processInstanceId);

  void delete(Long processDefinitionId, Long processInstanceId);

  void link(Long processDefinitionId, Long processInstanceId);

  void unlink(Long processDefinitionId, Long processInstanceId);

  HashMap<String, Object> saveAndLink(List<Data> columnsData, Long processDefinitionId, Long processInstanceId);

  void find(Map<String, List<Data>> filterDataByTableId, Long processInstanceId);
}
