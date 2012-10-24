package org.jbpm.ui.sync;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.dialog.UserInputDialog;
import org.jbpm.ui.pref.PrefConstants;
import org.jbpm.ui.resource.Messages;

import ru.runa.service.af.AuthenticationService;

public class WFEServerConnector implements IConnector, PrefConstants {

    static {
        Configuration.setConfiguration(new KerberosLoginConfiguration());
    }

    private InitialContext remoteContext;
    private Subject subject;

    private static WFEServerConnector instance;

    private String password;

    private WFEServerConnector() {
    }

    public static synchronized WFEServerConnector getInstance() {
        if (instance == null) {
            instance = new WFEServerConnector();
        }
        return instance;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public boolean isConfigured() {
        if (DesignerPlugin.getPrefString(P_CONNECTION_WFE_PROVIDER_URL).length() == 0) {
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
    public boolean connect() throws Exception {
        Hashtable<String, String> environment = new Hashtable<String, String>();
        String prop = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        if (prop == null) {
            prop = "org.jnp.interfaces.NamingContextFactory";
        }
        environment.put(Context.INITIAL_CONTEXT_FACTORY, prop);
        prop = System.getProperty(Context.URL_PKG_PREFIXES);
        if (prop == null) {
            prop = "org.jboss.naming:org.jnp.interfaces";
        }
        environment.put(Context.URL_PKG_PREFIXES, prop);
        environment.put(Context.PROVIDER_URL, DesignerPlugin.getPrefString(P_CONNECTION_WFE_PROVIDER_URL));
        remoteContext = new InitialContext(environment);
        AuthenticationService service = getService("AuthenticationServiceBean");
        if (LOGIN_MODE_LOGIN_PASSWORD.equals(DesignerPlugin.getPrefString(P_CONNECTION_LOGIN_MODE))) {
            String login = DesignerPlugin.getPrefString(P_CONNECTION_LOGIN);
            password = DesignerPlugin.getPrefString(P_CONNECTION_PASSWORD);
            if (password.length() == 0) {
                Display.getDefault().syncExec(new Runnable() {

                    public void run() {
                        UserInputDialog userInputDialog = new UserInputDialog(Messages.getString("pref.connection.password"), "");
                        if (Window.OK == userInputDialog.open()) {
                            password = userInputDialog.getUserInput();
                        }
                    }
                });
                if (password.length() == 0) {
                    return false;
                }
            }
            subject = service.authenticate(login, password);
        } else {
            GSSManager manager = GSSManager.getInstance();
            GSSCredential clientCred = manager.createCredential(GSSCredential.INITIATE_ONLY);
            GSSName peerName = manager.createName("WFServer", null);
            GSSContext context = manager.createContext(peerName, (Oid) null, clientCred, GSSContext.DEFAULT_LIFETIME);
            context.requestMutualAuth(false);
            byte[] token = new byte[0];
            token = context.initSecContext(token, 0, token.length);
            subject = service.authenticate(token);
        }
        return true;
    }

    @Override
    public void disconnect() throws Exception {
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(String beanName) throws Exception {
        String jndiName = beanName + "/remote";
        return (T) remoteContext.lookup(jndiName);
    }

}
