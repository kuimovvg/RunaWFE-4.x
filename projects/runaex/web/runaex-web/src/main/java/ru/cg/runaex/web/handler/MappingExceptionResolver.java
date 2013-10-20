package ru.cg.runaex.web.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 * @author Kochetkov
 */
public class MappingExceptionResolver extends SimpleMappingExceptionResolver {

  @Override
  protected ModelAndView doResolveException(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Object handler,
                                            Exception ex) {
    boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    if (isAjaxRequest) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      PrintWriter writer;
      try {
        writer = response.getWriter();
        writer.write(getExceptionMessage(ex));
        writer.flush();
      }
      catch (IOException e) {
        logger.error(ex.getMessage(), e);
      }
      return null;
    }
    else {
      return super.doResolveException(request, response, handler, ex);
    }
  }

  private String getExceptionMessage(Throwable e) {
    List<Throwable> throwableList = new ArrayList<Throwable>();
    String message = "";
    while (e != null) {
      message += e.getMessage() + "\n";
      e = e.getCause();
      if (throwableList.contains(e)) {
        break;
      }
      else {
        throwableList.add(e);
      }
    }
    return message;
  }

  protected ModelAndView getModelAndView(String viewName, Exception ex) {
    ModelAndView mv = new ModelAndView(viewName);
    mv.addObject(DEFAULT_EXCEPTION_ATTRIBUTE, getExceptionMessage(ex));
    return mv;
  }
}
