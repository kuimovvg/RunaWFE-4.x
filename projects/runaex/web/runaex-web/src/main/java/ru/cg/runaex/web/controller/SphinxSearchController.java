package ru.cg.runaex.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.cg.fias.search.core.server.bean.SearchSphinxResult;
import ru.cg.fias.search.core.server.component.converter.BaseConverter;
import ru.cg.fias.search.core.server.component.reader.SphinxBaseReader;
import ru.cg.fias.search.core.server.datasource.SphinxDataSource;
import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.database.util.SphinxSearchConvertObject;
import ru.cg.runaex.database.util.SphinxSearchUtil;
import ru.cg.runaex.web.json.ListMappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Абдулин Ильдар
 */
@Controller
public class SphinxSearchController {

  @Autowired
  @Qualifier("sphinxToolsSphinxDataSource")
  private SphinxDataSource sphinxDataSource;

  @Autowired
  private SphinxSearchUtil sphinxSearchUtil;

  @RequestMapping(value = "/sphinxSearch", method = RequestMethod.POST)
  public ModelAndView search(@RequestParam("query") String query, @RequestParam("indexName") String indexName, @RequestParam(value = "callback", required = false) String callback, HttpServletRequest request, HttpServletResponse response) throws SphinxException {

    SphinxBaseReader reader = new SphinxBaseReader();
    reader.setConverter(new BaseConverter());
    reader.setDataSource(sphinxDataSource);
    reader.setIndex(indexName + " " + indexName + "_delta");

    List<SearchSphinxResult> fList = reader.search(query);
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    if (fList != null && !fList.isEmpty()) {
      for (SearchSphinxResult searchSphinxResult : fList) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();

        SphinxSearchConvertObject ssConvertObject = sphinxSearchUtil.convert(searchSphinxResult, indexName);


        item.put("id", searchSphinxResult.getId().toString());
        item.put("value", ssConvertObject.getValue());
        item.put("ids", ssConvertObject.getIdMap());
        item.put("objects", ssConvertObject.getObjectMap());

        result.add(item);
      }
    }
    ModelAndView mv = new ModelAndView(new ListMappingJacksonJsonView());
    mv.addObject(result);


    return mv;
  }


}
