package ru.cg.runaex.web.enumrole;

/**
 * @author golovlyev
 */
public enum RoleEnum {
  ADMINISTRATORS("Administrators"),
  PROCESS_DEFINITION_ADMINISTRATORS("Process Definition Administrators");

  private final String role;

  RoleEnum(String role) {
    this.role = role;
  }

  public String getRole() {
    return this.role;
  }
}
