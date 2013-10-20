package ru.cg.runaex.web.bean.generate_users;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author golovlyev
 */
public class GeneratedUserInfo {
  private String fullName;
  private String login;
  private String password;

  @XmlElement
  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  @XmlElement
  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  @XmlElement
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
