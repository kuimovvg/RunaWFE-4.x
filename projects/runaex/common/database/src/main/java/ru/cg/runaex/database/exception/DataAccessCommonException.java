package ru.cg.runaex.database.exception;

import org.springframework.dao.DataAccessException;

/**
 * @author Kochetkov
 */
public class DataAccessCommonException extends DataAccessException {
  private static final long serialVersionUID = -2210410872149582625L;

  public DataAccessCommonException(String msg) {
    super(msg);
  }

  public DataAccessCommonException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
