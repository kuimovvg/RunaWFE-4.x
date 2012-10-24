package org.jbpm.ui.sync;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.eclipse.jface.window.Window;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.dialog.UserInputDialog;
import org.jbpm.ui.pref.PrefConstants;
import org.jbpm.ui.resource.Messages;

public class LDAPConnector implements IConnector, PrefConstants {

    private DirContext dirContext;

    private static LDAPConnector instance;

    private LDAPConnector() {
    }

    public static synchronized LDAPConnector getInstance() {
        if (instance == null) {
            instance = new LDAPConnector();
        }
        return instance;
    }

    public DirContext getDirContext() {
        return dirContext;
    }

    @Override
    public boolean connect() throws Exception {
        String serverURL = DesignerPlugin.getPrefString(P_CONNECTION_LDAP_SERVER_URL);
        String dc = DesignerPlugin.getPrefString(P_CONNECTION_LDAP_DC);
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, serverURL + "/" + dc);
        if (LOGIN_MODE_LOGIN_PASSWORD.equals(DesignerPlugin.getPrefString(P_CONNECTION_LOGIN_MODE))) {
            String login = DesignerPlugin.getPrefString(P_CONNECTION_LOGIN);
            String password = DesignerPlugin.getPrefString(P_CONNECTION_PASSWORD);
            if (password.length() == 0) {
                UserInputDialog userInputDialog = new UserInputDialog(Messages.getString("pref.connection.password"), "");
                if (Window.OK == userInputDialog.open()) {
                    password = userInputDialog.getUserInput();
                }
                if (password.length() == 0) {
                    return false;
                }
            }
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, login);
            env.put(Context.SECURITY_CREDENTIALS, password);
        } else {
            env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");
        }
        env.put("java.naming.ldap.version", "3");
        this.dirContext = new InitialDirContext(env);
        return true;
    }

    @Override
    public boolean isConfigured() {
        if (DesignerPlugin.getPrefString(P_CONNECTION_LDAP_SERVER_URL).length() == 0) {
            return false;
        }
        if (LOGIN_MODE_LOGIN_PASSWORD.equals(DesignerPlugin.getPrefString(P_CONNECTION_LOGIN_MODE))) {
            if (DesignerPlugin.getPrefString(P_CONNECTION_LOGIN).length() == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void disconnect() throws Exception {
        dirContext.close();
    }

}
