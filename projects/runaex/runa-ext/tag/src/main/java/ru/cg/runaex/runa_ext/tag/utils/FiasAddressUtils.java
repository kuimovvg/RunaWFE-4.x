package ru.cg.runaex.runa_ext.tag.utils;

import java.util.List;

import freemarker.template.TemplateModelException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.cg.fias.search.core.server.bean.AddressSphinx;
import ru.cg.fias.search.core.server.component.reader.SphinxAddressReader;
import ru.cg.fias.search.core.server.sphinx.SphinxException;
import ru.cg.runaex.database.context.DatabaseSpringContext;

/**
 * @author Абдулин Ильдар
 */
public class FiasAddressUtils {

  protected static final Log logger = LogFactory.getLog(FiasAddressUtils.class);

  public static String getAddressByGuId(String guId) throws TemplateModelException {
    List<AddressSphinx> foundList;
    try {
      foundList = DatabaseSpringContext.getSphinxStrAddressByGuidReader().search(guId, 1);
    }
    catch (SphinxException e) {
      logger.error(e.getMessage(), e);
      throw new TemplateModelException(e.toString(), e);
    }

    if (foundList.size() < 1) {
      return "";
    }

    return SphinxAddressReader.fullAddress(foundList.get(0));
  }

}
