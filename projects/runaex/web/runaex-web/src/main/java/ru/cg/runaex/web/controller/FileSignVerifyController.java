package ru.cg.runaex.web.controller;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;

import com.jul.sign.cryptopro.CryptoProSignUtils;
import com.jul.sign.cryptopro.exception.SignException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import ru.cg.runaex.components.util.FileUploadComponentHelper;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.generatedb.bean.Table;

/**
 * @author Kochetkov
 */
@Controller
public class FileSignVerifyController {
  private Logger logger = LoggerFactory.getLogger(FileSignVerifyController.class);

  @Autowired
  private BaseDao baseDao;

  @Autowired
  @Qualifier("signForFileMessageSource")
  private ResourceBundleMessageSource messages;

  @RequestMapping(value = "/fileSignVerify", method = RequestMethod.POST)
  public ModelAndView verify(@RequestParam("id") Long id,
                             @RequestParam("schema") String schema,
                             @RequestParam("table") String table,
                             @RequestParam("field") String field,
                             @RequestParam("processDefinitionId") Long processDefinitionId,
                             @RequestParam("signColumnName") String signColumnName,
                             HttpServletResponse response) {
    ModelAndView mv = new ModelAndView(new MappingJacksonJsonView());
    String verifyMsg = "";
    Boolean passedVerify = false;

    if (id != null) {
      Data filterData = new Data();
      filterData.setField(table.concat(Table.POSTFIX_TABLE_ID));
      filterData.setValue(id);
      TransportData filterTransportData = new TransportData(0, Arrays.asList(filterData));

      String dataColumn = FileUploadComponentHelper.getDataColumn(field);
      String[] fields = new String[] {signColumnName, dataColumn};
      TransportDataSet dataSet = baseDao.getData(processDefinitionId, schema, table, fields,
          0, 1, null, null, null, null, filterTransportData, null, null, null);

      TransportData transportData = dataSet.getSortSets().get(0);
      String sign = (String) transportData.getData(signColumnName).getValue();
      byte[] bytes = (byte[]) transportData.getData(dataColumn).getValue();
      String data = null;

      if (bytes != null) {
        data = new String(Base64.encodeBase64(bytes));
      }
      if (sign != null && data != null) {
        try {
          CryptoProSignUtils.verify(sign.getBytes("utf-8"), data);
          verifyMsg = messages.getMessage("signForFileVerified", null, Locale.ROOT);
          passedVerify = true;
        }
        catch (SignException e) {
          verifyMsg = messages.getMessage("signForFileWrong", null, Locale.ROOT);
        }
        catch (UnsupportedEncodingException e) {
          verifyMsg = messages.getMessage("verifyError", null, Locale.ROOT);
          logger.error(e.getMessage(), e);
        }
      }
    }
    else {
      verifyMsg = messages.getMessage("signNotFound", null, Locale.ROOT);
    }
    mv.addObject("verifyMsg", verifyMsg);
    mv.addObject("passedVerify", passedVerify);

    return mv;
  }
}
