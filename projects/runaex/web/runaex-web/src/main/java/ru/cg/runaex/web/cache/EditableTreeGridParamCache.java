package ru.cg.runaex.web.cache;

import javax.servlet.http.HttpSession;

import ru.cg.runaex.database.bean.FullTableParam;

/**
 * @author Абдулин Ильдар
 */
public interface EditableTreeGridParamCache {

  public FullTableParam getEditableTreeGridParam(String tableId, HttpSession session);

  public void saveEditableTreeGridParam(String tableId, FullTableParam editableTreeGridParam, HttpSession session);
}
