package ru.cg.runaex.runa_ext.tag.field;

import freemarker.template.TemplateModelException;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.IsComponent;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.runa_ext.tag.BaseFreemarkerTag;

import java.util.Map;

/**
 * @author Петров А.
 */
public abstract class BaseFieldFreemarkerTag<C extends IsComponent> extends BaseFreemarkerTag<C> {
  private static final long serialVersionUID = -6350812038940960094L;

  protected ObjectInfo getObjectInfo(String schema, String table) throws TemplateModelException {
    Map<Long, ObjectInfo> objectInfoMap = (Map<Long, ObjectInfo>) variableProvider.getValueNotNull(WfeRunaVariables.OBJECT_INFO);

    for (ObjectInfo tmpObjectInfo : objectInfoMap.values()) {
      if (!WfeRunaVariables.isEmpty(schema) && schema.equals(tmpObjectInfo.getSchema()) &&
          !WfeRunaVariables.isEmpty(table) && table.equals(tmpObjectInfo.getTable())) {
        return tmpObjectInfo;
      }
    }

    return null;
  }

  protected <T> T copyValue(ColumnReference columnReference, Class<T> clazz, T defaults) throws TemplateModelException {
    String refSchema = columnReference.getSchema();
    String refTable = columnReference.getTable();
    String refColumn = columnReference.getColumn();

    ObjectInfo refObjectInfo = getObjectInfo(refSchema, refTable);
    if (refObjectInfo != null) {
      return getValue(refObjectInfo.getId(), refSchema, refTable, refColumn, null, clazz, defaults);
    }

    return null;
  }
}
