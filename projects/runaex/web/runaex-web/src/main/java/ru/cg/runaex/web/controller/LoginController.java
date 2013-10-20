package ru.cg.runaex.web.controller;

import java.io.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ru.cg.runaex.web.model.Principal;
import ru.cg.runaex.web.utils.SessionUtils;

/**
 * @author Петров А.
 */
@Controller
public class LoginController {

  @RequestMapping(value = "/login", method = RequestMethod.GET)
  public ModelAndView handleGetRequest() throws IOException {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "login");
    mv.addObject("principal", new Principal());
    mv.addObject("projectName", SessionUtils.getProjectName());
    return mv;
  }
}
