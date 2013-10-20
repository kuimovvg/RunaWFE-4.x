package ru.cg.runaex.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import ru.cg.runaex.database.bean.TemplateUploadStatus;
import ru.cg.runaex.database.bean.model.ReportTemplate;
import ru.cg.runaex.database.dao.ReportDao;
import ru.cg.runaex.web.model.FlexiGridResult;
import ru.cg.runaex.web.utils.FlexiGridHelper;

/**
 * @author Kochetkov
 */
@Controller
public class ManageReportTemplateController extends BaseController {

  private static final Logger logger = LoggerFactory.getLogger(ManageProcessDefinitionsController.class);

  @Autowired
  @Qualifier("reportTemplateMessageSource")
  private ResourceBundleMessageSource messageSource;

  @Autowired
  private ReportDao reportDao;

  @RequestMapping(value = "/reportTemplates", method = RequestMethod.GET)
  public ModelAndView reportTemplates() {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "report_templates");
    return mv;
  }

  @RequestMapping(value = "/uploadReportTemplates", method = RequestMethod.GET)
  public ModelAndView uploadReportTemplates() {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "upload_report_template");
    return mv;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/uploadTemplateFiles", method = RequestMethod.POST)
  public ModelAndView uploadTemplateFiles(@RequestParam(value = "parentTemplateName", required = false) String parentTemplateName,
                                          MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, MultipartFile> fileMap = request.getFileMap();
    ModelAndView mv = new ModelAndView(new MappingJacksonJsonView());

    Map<String, byte[]> templateFilesToSave = new LinkedHashMap<String, byte[]>();
    if (!fileMap.isEmpty()) {
      for (String fileName : fileMap.keySet()) {
        MultipartFile templateFile = fileMap.get(fileName);
        if (templateFile.getSize() == 0) {
          continue;
        }
        String originalName = templateFile.getOriginalFilename();
        if (!(originalName.endsWith(".jasper"))) {
          responseErrorMessage(response, "jasperFileFormat");
          return mv;
        }
        else {
          String templateName = originalName.substring(0, originalName.lastIndexOf(".jasper"));
          templateFilesToSave.put(templateName, templateFile.getBytes());
        }
      }

      Map<TemplateUploadStatus, Object> results = reportDao.saveReportTemplate(templateFilesToSave, parentTemplateName);
      int successUploaded = (Integer) results.get(TemplateUploadStatus.SUCCESS);
      List<String> uniqueViolation = (List<String>) results.get(TemplateUploadStatus.UNIQUE_NAME_VIOLATION);
      Boolean unknownError = (Boolean) results.get(TemplateUploadStatus.UNKNOWN_ERROR);

      StringBuilder info = new StringBuilder();
      info.append(messageSource.getMessage("successUploaded", null, Locale.ROOT)).append(successUploaded).append("\n");
      if (uniqueViolation != null) {
        info.append(messageSource.getMessage("uniqueViolation", null, Locale.ROOT)).append("\n");
        for (int i = 0; i < uniqueViolation.size(); i++) {
          info.append(uniqueViolation.get(i));
          if (i + 1 != uniqueViolation.size()) {
            info.append(",\n");
          }
        }
      }

      if (unknownError) {
        int notUploadedForUnknownError = fileMap.size() - successUploaded;
        if (uniqueViolation != null) {
          notUploadedForUnknownError -= uniqueViolation.size();
        }
        info.append(messageSource.getMessage("unknownError", null, Locale.ROOT)).append(notUploadedForUnknownError);
      }
      mv.addObject("info", info.toString());
    }
    else {
      responseErrorMessage(response, "emptyTemplateFiles");
    }
    return mv;
  }

  private void responseErrorMessage(HttpServletResponse response, String key) throws IOException {
    String error = messageSource.getMessage(key, new Object[] {}, Locale.ROOT);
    if (error == null)
      return;
    response.setStatus(420);
    PrintWriter writer = response.getWriter();
    writer.write(error);
    writer.flush();
  }

  protected ResourceBundleMessageSource getMessages(){
    return messageSource;
  }

  @ResponseBody
  @RequestMapping(value = "/load-templates", method = RequestMethod.POST)
  public FlexiGridResult loadData(
      @RequestParam("page") Integer page,
      @RequestParam("rp") Integer pageSize,
      @RequestParam("sortname") String sortName,
      @RequestParam("sortorder") String sortOrder) {

    int total = reportDao.getTemplatesCount();
    List<ReportTemplate> users = reportDao.loadTemplates(sortName, sortOrder, (page - 1) * pageSize, pageSize);
    return FlexiGridHelper.convert(page, total, users, new FlexiGridHelper.RowMapper<ReportTemplate>() {
      @Override
      public FlexiGridResult.Row mapRow(ReportTemplate model) {
        List<String> cells = new ArrayList<String>(1);
        cells.add(model.getTemplateName());
        return new FlexiGridResult.Row(model.getId(), cells);
      }
    });
  }

  @RequestMapping(value = "/delete-templates", method = RequestMethod.POST)
  public ModelAndView deleteTemplates(@RequestParam("templateIds") String templateIds,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
    ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
    String[] idArray = templateIds.split(",");
    Long[] reportTemplateIds = new Long[idArray.length];
    int i = 0;
    for (String id : idArray) {
      reportTemplateIds[i] = Long.parseLong(id);
      i++;
    }
    reportDao.deleteTemplatesByIds(reportTemplateIds);
    modelAndView.addObject("autohide", messageSource.getMessage("successDeleteTemplateMsg", null, Locale.ROOT));
    return modelAndView;
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
