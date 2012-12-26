package ru.runa.alfresco;

import org.alfresco.webservice.authentication.AuthenticationFault;
import org.alfresco.webservice.repository.RepositoryFault;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.axis.AxisFault;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.ApplicationException;

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
        } catch (RepositoryFault e) {
            log.error(e.getMessage1());
            throw new ApplicationException(e.getMessage1());
        } catch (AxisFault e) {
            String s = getDetail(e);
            log.error(e.dumpToString());
            throw new RuntimeException(s);
        } catch (Exception e) {
            if (ConnectionException.MESSAGE.equals(e.getMessage())) {
                throw new ConnectionException();
            }
            throw new RuntimeException(e);
        } finally {
            sessionEnd();
        }
    }

    protected abstract T code() throws Exception;

    private String getDetail(AxisFault e) {
        if (e.getFaultDetails().length > 0) {
            return XMLUtils.getInnerXMLString(e.getFaultDetails()[0]);
        }
        return e.dumpToString();
    }

    static class SessionData {
        final int id = sessionIdCounter++;
        int level = 0;
    }
}
