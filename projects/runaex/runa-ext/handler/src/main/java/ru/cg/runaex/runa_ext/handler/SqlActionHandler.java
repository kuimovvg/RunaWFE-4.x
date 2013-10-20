/*
 * Copyright (c) 2012.
 *
 * Class: GuidActionHandler
 * Last modified: 24.09.12 14:06
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.handler.SQLActionHandler;

public class SqlActionHandler extends SQLActionHandler {
  private static final Log log = LogFactory.getLog(SqlActionHandler.class);
  private static final String EMPTY_ROW_SET_MESSAGE = "ResultSet not positioned properly, perhaps you need to call next.";


  @Override
  public void execute(ExecutionContext executionContext) {
    try {
      super.execute(executionContext);
    }
    catch (RuntimeException ex) {
      if (!ex.getMessage().contains(EMPTY_ROW_SET_MESSAGE)) {
        log.error(ex);
        throw ex;
      }
    }
    catch (Exception e) {
      log.error(e);
    }
  }
}