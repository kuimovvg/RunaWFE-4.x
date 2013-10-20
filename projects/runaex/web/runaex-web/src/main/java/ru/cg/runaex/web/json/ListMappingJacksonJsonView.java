package ru.cg.runaex.web.json;

import java.util.Map;

import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * Вариация {@link MappingJacksonJsonView}. Если в ModelAndView один объект,
 * то вернется только он, а не Map.
 *
 * Пример:
 * ModelAndView mav = new ModelAndView();
 * mav.addObject(listOfObjects);
 * return mav;
 *
 * вернет
 * [{"name":"object1"}, {"name":"object2"}]
 *
 * вместо
 * {"objectList" : [{"name":"object1"}, {"name":"object2"}]}
 * в стандатрной реализации.
 *
 * @author Петров А.
 */
public class ListMappingJacksonJsonView extends MappingJacksonJsonView {

  @Override
  protected Object filterModel(Map<String, Object> model) {
    Object result = super.filterModel(model);
    if (!(result instanceof Map)) {
      return result;
    }

    Map map = (Map) result;
    if (map.size() == 1) {
      return map.values().toArray()[0];
    }
    return map;
  }
}
