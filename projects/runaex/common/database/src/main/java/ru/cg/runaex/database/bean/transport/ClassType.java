package ru.cg.runaex.database.bean.transport;

/**
 * Date: 17.08.12
 * Time: 15:37
 *
 * @author Sabirov
 */
public enum ClassType {
  /**
   * from class simple name
   */
  LONG("Long"),
  STRING("String"),
  INTEGER("Integer"),
  BOOLEAN("Boolean"),
  DATE("DATE"),
  DATETIME("DATETIME"),
  BYTEARRAY("byte[]"),
  /**
   * sql column type name from meta data
   */
  INT8("int8"),
  INT4("int4"),
  VARCHAR("varchar"),
  BOOL("bool"),
  TIMESTAMP("timestamp"),
  TIMESTAMP_WITH_TIMEZONE("timestamptz", new String[] {"TIMESTAMP WITH TIME ZONE"}),
  BYTEA("bytea"),
  BIG_DECIMAL("BigDecimal", new String[] {"numeric"});

  private String simpleName;
  private String[] aliases;


  ClassType(String simpleName) {
    this(simpleName, null);
  }

  ClassType(String simpleName, String[] aliases) {
    this.simpleName = simpleName;
    this.aliases = aliases;
  }

  public static ClassType valueOfBySimpleName(String simpleName) {
    for (ClassType classType : ClassType.values()) {
      if (simpleName != null) {
        String simpleNameInLowerCase = simpleName.toLowerCase();
        if (classType.getSimpleName().toLowerCase().equals(simpleNameInLowerCase)) {
          return classType;
        }
        if (classType.getAliases() != null) {
          for (String alias : classType.getAliases()) {
            if (simpleNameInLowerCase.equals(alias.toLowerCase())) {
              return classType;
            }
          }
        }
      }
    }
    return STRING;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public void setSimpleName(String simpleName) {
    this.simpleName = simpleName;
  }

  public String[] getAliases() {
    return aliases;
  }

  public void setAliases(String[] aliases) {
    this.aliases = aliases;
  }


}
