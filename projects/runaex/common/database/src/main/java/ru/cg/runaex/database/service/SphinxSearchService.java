package ru.cg.runaex.database.service;

import ru.cg.runaex.database.bean.model.SphinxSearchIndexingAndViewColumns;

/**
 * @author Абдулин Ильдар
 */
public interface SphinxSearchService {
  public SphinxSearchIndexingAndViewColumns getIndexingAndViewColumnsByIndexName(String indexName);
}
