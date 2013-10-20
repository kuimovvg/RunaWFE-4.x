package ru.cg.runaex.web.cache;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import ru.cg.runaex.database.bean.FullTableParam;

/**
 * @author Абдулин Ильдар
 */
@Component
public class EditableTreeGridParamCacheImpl implements EditableTreeGridParamCache {

  public String SESSION_KEY = "EDITABLE_TREE_GRID_PARAM_CACHE_SESSION_KEY";

  @Override
  public FullTableParam getEditableTreeGridParam(String tableId, HttpSession session) {
    if (session.getAttribute(SESSION_KEY) == null) {
      session.setAttribute(SESSION_KEY, createMap());
    }

    Map<String, FullTableParam> params = (Map<String, FullTableParam>) session.getAttribute(SESSION_KEY);
    FullTableParam editableTreeGridParam = params.get(tableId);
    if (editableTreeGridParam == null) {
      editableTreeGridParam = new FullTableParam();
      params.put(tableId, editableTreeGridParam);
    }
    return editableTreeGridParam;
  }

  @Override
  public void saveEditableTreeGridParam(String tableId, FullTableParam editableTreeGridParam, HttpSession session) {
    if (session.getAttribute(SESSION_KEY) == null) {
      session.setAttribute(SESSION_KEY, createMap());
    }
    Map<String, FullTableParam> params = (Map<String, FullTableParam>) session.getAttribute(SESSION_KEY);
    params.put(tableId, editableTreeGridParam);
  }

  protected Map<String, FullTableParam> createMap() {
    return new HashMap<String, FullTableParam>();
  }
}
