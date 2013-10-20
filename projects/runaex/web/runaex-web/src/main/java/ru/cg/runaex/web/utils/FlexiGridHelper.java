package ru.cg.runaex.web.utils;

import java.util.ArrayList;
import java.util.List;

import ru.cg.runaex.web.model.FlexiGridResult;

/**
 * @author urmancheev
 */
public final class FlexiGridHelper {

  public static <M> FlexiGridResult convert(Integer page, Integer total, List<M> models, RowMapper<M> mapper) {
    List<FlexiGridResult.Row> rows = new ArrayList<FlexiGridResult.Row>(models.size());
    for (M model : models) {
      rows.add(mapper.mapRow(model));
    }
    FlexiGridResult result = new FlexiGridResult();
    result.setPage(page);
    result.setTotal(total);
    result.setRows(rows);
    return result;
  }

  public interface RowMapper<M> {
    public FlexiGridResult.Row mapRow(M model);
  }

}
