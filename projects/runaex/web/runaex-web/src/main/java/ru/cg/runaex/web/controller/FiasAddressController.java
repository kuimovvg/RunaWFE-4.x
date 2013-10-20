package ru.cg.runaex.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ru.cg.fias.search.core.client.bean.ObjectLevel;
import ru.cg.fias.search.core.server.bean.AddressSphinx;
import ru.cg.fias.search.core.server.component.reader.SphinxAddressReader;
import ru.cg.fias.search.core.server.component.reader.SphinxHistoricAddressReader;
import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.web.json.ListMappingJacksonJsonView;


/**
 * @author Абдулин Ильдар
 */
@Controller
public class FiasAddressController {

  private static final Logger logger = LoggerFactory.getLogger(FiasAddressController.class);

  @Autowired
  private SphinxAddressReader sphinxAddressReader;

  @Autowired
  private SphinxHistoricAddressReader sphinxHistoricAddressReader;

  @Autowired
  @Qualifier("runaexMessageSource")
  private ResourceBundleMessageSource messages;

  @RequestMapping(value = "/searchAddress", method = RequestMethod.POST)
  public ModelAndView search(@RequestParam("query") String query,
                             @RequestParam("minLevel") Integer minLevel,
                             @RequestParam("maxLevel") Integer maxLevel,
                             @RequestParam("defaultFilter") String defaultFilter,
                             @RequestParam(value = "historical") Boolean historical,
                             @RequestParam(value = "historicDate", required = false)
                             Date date,
                             HttpServletResponse response) throws IOException {

    List<AddressSphinx> list = null;
    try {
      if (!historical)
        list = sphinxAddressReader.search(defaultFilter + ", " + query, ObjectLevel.valueOf(minLevel), ObjectLevel.valueOf(maxLevel));
      else
        list = sphinxHistoricAddressReader.search(defaultFilter + ", " + query, date, ObjectLevel.valueOf(minLevel), ObjectLevel.valueOf(maxLevel));
    }
    catch (SphinxException ex) {
      logger.error(ex.toString(), ex);
      String error = messages.getMessage("sphinxError", null, Locale.ROOT);
      response.setStatus(420);
      PrintWriter writer = response.getWriter();
      writer.write(error);
      writer.flush();
    }
    if (list == null) {
      logger.error("sphinxAddressReaderps a return null");
      list = new ArrayList<AddressSphinx>();
    }
    List<Map<String, String>> result = new ArrayList<Map<String, String>>();
    for (AddressSphinx addressSphinx : list) {
      String fullAddress = SphinxAddressReader.fullAddress(addressSphinx);

      Map<String, String> item = new HashMap<String, String>();
      if (!historical) {
        item.put("id", addressSphinx.<String>get("guid"));
      }
      else {
        item.put("id", addressSphinx.<String>get("aoid"));
      }
      item.put("label", fullAddress);
      item.put("value", fullAddress);
      result.add(item);
    }

    ModelAndView mv = new ModelAndView(new ListMappingJacksonJsonView());
    mv.addObject(result);

    return mv;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    dateFormat.setLenient(false);
    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
  }

  private static boolean isEmpty(String obj) {
    return obj == null || obj.isEmpty();
  }
}
