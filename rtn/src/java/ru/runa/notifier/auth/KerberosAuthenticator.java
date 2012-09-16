package ru.runa.notifier.auth;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import ru.runa.af.service.AuthenticationService;
import ru.runa.delegate.DelegateFactory;

public class KerberosAuthenticator implements Authenticator {
    private static final Log log = LogFactory.getLog(KerberosAuthenticator.class);

    public Subject authenticate() throws Exception {
        AuthenticationService authenticationService = DelegateFactory.getInstance().getAuthenticationService();
        GSSManager manager = GSSManager.getInstance();
        GSSCredential clientCred = manager.createCredential(GSSCredential.INITIATE_ONLY);
        if (log.isDebugEnabled()) {
            log.debug("Trying to authenticate as " + clientCred.getName());
        }
        GSSName peerName = manager.createName(LoginModuleResources.getServerPrincipal(), null);
        GSSContext context = manager.createContext(peerName, (Oid) null, clientCred, GSSContext.DEFAULT_LIFETIME);
        context.requestMutualAuth(false);

        byte[] token = new byte[0];
        token = context.initSecContext(token, 0, token.length);
        return authenticationService.authenticate(token);
    }
    
    public String getParamForWeb(){
    	return "";
    }

    public boolean isRetryDialogEnabled(){
    	return false;
    }
}
