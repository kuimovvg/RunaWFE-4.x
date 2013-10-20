package ru.cg.runaex.database.util;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.cg.fias.search.core.server.bean.SearchSphinxResult;
import com.cg.sphinx_tools.core.utils.Translit;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;

/**
 * @author Абдулин Ильдар
 */
@Component
public class SphinxSearchUtil {

  private static class Column {
    public String column;
    public Boolean isId;

    public Column(String column, Boolean id) {
      this.column = column;
      isId = id;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Column && column != null && obj != null && ((Column) obj).column != null && column.toUpperCase().equals(((Column) obj).column.toUpperCase());
    }
  }

  @Autowired
  private BaseDao baseDao;

  private final Map<String, List<Column>> cache = new LinkedHashMap<String, List<Column>>();

  public void refreshCache() {
    String[] selectFields = new String[3];
    selectFields[0] = "index";
    selectFields[1] = "columns";
    selectFields[2] = "is_id";
    TransportDataSet resultData = baseDao.getData(null, "metadata", "sphinx_search", selectFields, null, null, null, null,
        null, null, null, "dummy", null, null);

    Map<String, List<Column>> newCache = new HashMap<String, List<Column>>();
    for (TransportData td : resultData.getSets()) {
      String index = (String) td.getData("index").getValue();
      String[] columns = (String[]) td.getData("columns").getValue();
      String[] isId = (String[]) td.getData("is_id").getValue();
      List<Column> cacheList = new ArrayList<Column>();
      for (int i = 0; i < columns.length; i++) {
        cacheList.add(new Column(columns[i], "Y" .equals(isId[i].toUpperCase())));
      }
      newCache.put(index, cacheList);
    }

    synchronized (cache) {
      cache.clear();
      cache.putAll(newCache);
    }
  }

  public SphinxSearchConvertObject convert(SearchSphinxResult searchSphinxResult, String indexName) {
    StringBuilder stringBuilder = new StringBuilder();
    Map<String, String> idMap = new LinkedHashMap<String, String>();
    Map<String, String> objectMap = new LinkedHashMap<String, String>();
    if (searchSphinxResult.getAll() != null) {
      List<Column> list = cache.get(indexName);
      if (list == null) {
        refreshCache();
        list = cache.get(indexName);
      }
      Map<String, Object> res = new LinkedHashMap<String, Object>();
      for (String key : searchSphinxResult.getAll().keySet()) {
        res.put(Translit.reTranslit(key), searchSphinxResult.get(key));
      }
      boolean needRefreshCache = false;
      for (String column : res.keySet()) {
        if (!list.contains(new Column(column, null))) {
          needRefreshCache = true;
          break;
        }
      }
      if (needRefreshCache) {
        refreshCache();
      }
      for (Column c : list) {
        Object value = res.get(c.column.toUpperCase());
        if (value != null) {
          if (c.isId) {
            idMap.put(c.column, value.toString());
            continue;
          }
          objectMap.put(c.column, value.toString());
          stringBuilder.append(value.toString()).append(" ");
        }
      }
    }

    return new SphinxSearchConvertObject(stringBuilder.toString(), idMap, objectMap);
  }


}
