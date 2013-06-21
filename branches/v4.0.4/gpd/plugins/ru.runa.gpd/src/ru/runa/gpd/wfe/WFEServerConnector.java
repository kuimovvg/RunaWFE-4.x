package ru.runa.gpd.wfe;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.Configuration;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.user.User;

import com.google.common.base.Throwables;

public class WFEServerConnector implements IConnector, PrefConstants {
    static {
        Configuration.setConfiguration(new KerberosLoginConfiguration());
    }
    private InitialContext remoteContext;
    private User user;
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

    public User getUser() {
        return user;
    }

    @Override
    public boolean isConfigured() {
        if (Activator.getPrefString(P_WFE_CONNECTION_PROVIDER_URL).length() == 0) {
            return false;
        }
        if (LOGIN_MODE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_WFE_CONNECTION_LOGIN_MODE))) {
            if (Activator.getPrefString(P_WFE_CONNECTION_LOGIN).length() == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void connect() {
        try {
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
            environment.put(Context.PROVIDER_URL, Activator.getPrefString(P_WFE_CONNECTION_PROVIDER_URL));
            remoteContext = new InitialContext(environment);
            AuthenticationService service = getService("AuthenticationServiceBean");
            if (LOGIN_MODE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_WFE_CONNECTION_LOGIN_MODE))) {
                String login = Activator.getPrefString(P_WFE_CONNECTION_LOGIN);
                password = Activator.getPrefString(P_WFE_CONNECTION_PASSWORD);
                if (password.length() == 0) {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            UserInputDialog userInputDialog = new UserInputDialog(Localization.getString("pref.connection.password"));
                            if (Window.OK == userInputDialog.open()) {
                                password = userInputDialog.getUserInput();
                            }
                        }
                    });
                    if (password.length() == 0) {
                        PluginLogger.logInfo("[wfeconnector] empty password");
                        return;
                    }
                }
                user = service.authenticateByLoginPassword(login, password);
            } else {
                GSSManager manager = GSSManager.getInstance();
                GSSCredential clientCred = manager.createCredential(GSSCredential.INITIATE_ONLY);
                GSSName peerName = manager.createName("WFServer", null);
                GSSContext context = manager.createContext(peerName, (Oid) null, clientCred, GSSContext.DEFAULT_LIFETIME);
                context.requestMutualAuth(false);
                byte[] token = new byte[0];
                token = context.initSecContext(token, 0, token.length);
                user = service.authenticateByKerberos(token);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void disconnect() throws Exception {
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(String beanName) {
        String jndiName = "runawfe/" + beanName + "/remote";
        try {
            return (T) remoteContext.lookup(jndiName);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to locale EJB by name " + jndiName, e);
        }
    }
}
