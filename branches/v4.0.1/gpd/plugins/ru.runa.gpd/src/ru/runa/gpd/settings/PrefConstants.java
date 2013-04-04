package ru.runa.gpd.settings;

/**
 * Constant definitions for plug-in preferences
 */
public interface PrefConstants {

    public final static String LOGIN_MODE_LOGIN_PASSWORD = "login.mode.login_password";
    public final static String LOGIN_MODE_KERBEROS = "login.mode.kerberos";

    public static final String P_BPMN_SHOW_SWIMLANE = "showSwimlane";

    public static final String P_DEFAULT_NOTATION = "defaultNotation";

    public static final String P_FORM_DEFAULT_FCK_EDITOR = "defaultFCKEditor";
    public static final String FORM_FCK_EDITOR = "fck2";
    public static final String FORM_CK_EDITOR = "ck3";

    public static final String P_FORM_USE_EXTERNAL_EDITOR = "useExternalEditor";

    public static final String P_FORM_EXTERNAL_EDITOR_PATH = "externalEditorPath";

    //public static final String P_TASKS_TIMEOUT_ENABLED = "useTasksTimeout";
    public static final String P_ESCALATION_DURATION = "escalationDuration";
    //public static final String P_TASKS_TIMEOUT_ACTION_CLASS = "tasksTimeoutActionClass";
    public static final String P_ESCALATION_CONFIG = "escalationConfig";
    public static final String P_ESCALATION_REPEAT = "escalationRepeat";

    public static final String P_CONNECTION_LOGIN = "wfeLogin";
    public static final String P_CONNECTION_PASSWORD = "wfePassword";
    public static final String P_CONNECTION_LOGIN_MODE = "wfeLoginMode";

    public static final String P_CONNECTION_WFE_PROVIDER_URL = "wfeProviderUrl";
    public static final String P_CONNECTION_WFE_INITIAL_CTX_FACTORY = "wfeInitialCtxFactory";
    public static final String P_CONNECTION_WFE_URL_PKG_PREFIXES = "wfeUrlPkg";

    public final static String P_CONNECTION_LDAP_SERVER_URL = "ldapServerUrl";
    public final static String P_CONNECTION_LDAP_OU = "ldapOu";
    public final static String P_CONNECTION_LDAP_DC = "ldapDc";

}
