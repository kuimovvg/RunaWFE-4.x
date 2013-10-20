package ru.cg.runaex.web.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import ru.cg.runaex.web.service.CreateUsersSevice;

/**
 * @author golovlyev
 */
@Controller
public class CreateUsersController extends BaseController {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private CreateUsersSevice createUsersSevice;

  /**
   * Go to the browse file page
   */
  @RequestMapping(value = "/createUsers", method = RequestMethod.GET)
  public ModelAndView changeLogotype() throws IOException {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "users_creator");
    return mv;
  }

  /**
   * Save users
   */
  @RequestMapping(value = "/generateUsers", method = RequestMethod.POST)
  public void generateUsers(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      List<MultipartFile> files = request.getFiles("fileUploader");
      removeEmptyFiles(files);
      if (!files.isEmpty()) {
        createUsersSevice.generateUsers(files, response);
      }
    }
    catch (Exception ex) {
      logger.error(ex.toString(), ex);
      responseErrorMessage(response, ex, ex.getMessage());
    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}