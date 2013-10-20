package ru.cg.runaex.web.utils;

import java.io.*;
import javax.servlet.http.HttpSession;

/**
 * @author Петров А.
 */
public final class SessionUtils {
  public static String pathToDir = System.getProperty("user.home") + File.separator + "runa-name";
  private static final String ACTOR_PRINCIPAL_SUFFIX = "actorprincipal";
  private static final String GROUPS_SUFFIX = "groups";

  /**
   * Default session maxAge. If this parameter not specified in application.conf
   */
  private static final Integer DEFAULT_SESSION_MAXAGE = 86400;

  private SessionUtils() {
  }

  public static String getUserGroupsKey(HttpSession session) {
    return session.getId().concat(GROUPS_SUFFIX);
  }

  public static String getActorPrincipalKey(HttpSession session) {
    return session.getId().concat(ACTOR_PRINCIPAL_SUFFIX);
  }

  public static String getKeyWithinSession(HttpSession session, String key) {
    return session.getId().concat(key);
  }

  public static String getProjectName() throws IOException {
    File file = new File(pathToDir + File.separator + "projectName.dat");
    String name = null;
    if (file.exists()) {
      name = "";
      FileInputStream fileInputStream = new FileInputStream(file);
      InputStreamReader streamReader = new InputStreamReader(fileInputStream);
      BufferedReader reader = new BufferedReader(streamReader);
      while (reader.ready()) {
        name += reader.readLine();
      }
      streamReader.close();
      fileInputStream.close();
    }
    return name;
  }
}
