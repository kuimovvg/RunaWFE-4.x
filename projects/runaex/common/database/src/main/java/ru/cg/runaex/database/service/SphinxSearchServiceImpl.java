package ru.cg.runaex.database.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.cg.runaex.database.bean.model.SphinxSearchIndexingAndViewColumns;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.dao.BaseDao;

/**
 * @author Абдулин Ильдар
 */
@Service("sphinxSearchService")
public class SphinxSearchServiceImpl implements SphinxSearchService {

  @Autowired
  private BaseDao baseDao;


  @Override
  public SphinxSearchIndexingAndViewColumns getIndexingAndViewColumnsByIndexName(String indexName) {
    String[] selectFields = new String[2];
    selectFields[0] = "indexing_columns";
    selectFields[1] = "view_columns";

    List<Data> dataList = new ArrayList<Data>(1);
    dataList.add(new Data("index_in", indexName, "varchar"));
    TransportData filterData = new TransportData(0, dataList);

    TransportDataSet resultData = DatabaseSpringContext.getBaseDao().getData(null, "metadata", "sphinx_search", selectFields, null, null, null, null,
        null, null, filterData, "dummy", null, null);

    for (TransportData td : resultData.getSets()) {
      SphinxSearchIndexingAndViewColumns indexingAndViewColumns = new SphinxSearchIndexingAndViewColumns();
      indexingAndViewColumns.setIndexingColumns((String) td.getData("indexing_columns").getValue());
      indexingAndViewColumns.setViewColumns((String) td.getData("view_columns").getValue());
      return indexingAndViewColumns;
    }
    return null;
  }
}
