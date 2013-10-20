package ru.cg.runaex.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartFile;

import ru.cg.runaex.database.provider.TransactionManagerProvider;


/**
 * @author Петров А.
 */
public abstract class BaseController {

  @Autowired
  @Qualifier("runaexMessageSource")
  protected ResourceBundleMessageSource messages;

  @Autowired
  protected TransactionManagerProvider transactionManagerProvider;

  protected void removeEmptyFiles(List<MultipartFile> files) {
    for (MultipartFile file : new ArrayList<MultipartFile>(files)) {
      if (file.getSize() == 0) {
        files.remove(file);
      }
    }
  }

  protected void responseErrorMessage(HttpServletResponse response, Exception ex, String key, Object... objects) throws IOException {
    getLogger().error(ex.toString(), ex);
    String error = getMessages().getMessage(key, objects, Locale.ROOT);
    if (error == null)
      return;
    response.setStatus(420);
    PrintWriter writer = response.getWriter();
    writer.write(error);
    writer.flush();
  }

  protected ResourceBundleMessageSource getMessages() {
    return messages;
  }


  protected abstract Logger getLogger();
}
