package ru.runa.alfresco;

import org.alfresco.webservice.authentication.AuthenticationFault;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Authenticated wrapper for {@link AlfSession}.
 * 
 * @author dofs
 */
public abstract class AlfSessionWrapper<T> {
    protected static Log log = LogFactory.getLog(AlfSessionWrapper.class);
    protected final AlfSession session = new AlfSession();
    private static int sessionIdCounter = 0;
    private static ThreadLocal<SessionData> sessions = new ThreadLocal<SessionData>();

    static {
        WebServiceFactory.setEndpointAddress(WSConnectionSettings.getEndpointAddress());
        WebServiceFactory.setTimeoutMilliseconds(7 * 60000);
    }

    protected static void log(String message) {
        log.debug(sessions.get().id + "(" + sessions.get().level + "): " + message);
    }

    public static void sessionStart() throws AuthenticationFault {
        if (sessions.get() == null) {
            AuthenticationUtils.startSession(WSConnectionSettings.getSystemLogin(), WSConnectionSettings.getSystemPassword());
            sessions.set(new SessionData());
            log("Started new session");
        }
        sessions.get().level++;
    }

    public static void sessionEnd() {
        if (sessions.get() != null) {
            sessions.get().level--;
            if (sessions.get().level == 0) {
                log("Ending session");
                AuthenticationUtils.endSession();
                sessions.remove();
            }
        }
    }

    public final T runInSession() {
        try {
            sessionStart();
            return code();
        } catch (Exception e) {
            if (ConnectionException.MESSAGE.equals(e.getMessage())) {
                throw new ConnectionException();
            }
            throw AlfSession.propagate(e);
        } finally {
            sessionEnd();
        }
    }

    protected abstract T code() throws Exception;

    static class SessionData {
        final int id = sessionIdCounter++;
        int level = 0;
    }
}
