package org.jbpm.ui.orgfunctions;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.pref.PrefConstants;
import org.jbpm.ui.sync.LDAPConnector;

public class LDAPExecutorsImporter extends ExecutorsImporter implements PrefConstants {

    private static final String OBJECT_CLASS_ATTR_NAME = "objectClass";
    private static final String OBJECT_CLASS_ATTR_USER_VALUE = "user";
    private static final String OBJECT_CLASS_ATTR_GROUP_VALUE = "group";
    private static final String SAM_ACCOUNT_NAME = "SamAccountName";

    private static LDAPExecutorsImporter instance;

    private LDAPExecutorsImporter() {
        super(LDAPConnector.getInstance());
    }

    public static synchronized LDAPExecutorsImporter getInstance() {
        if (instance == null) {
            instance = new LDAPExecutorsImporter();
        }
        return instance;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        for (String userName : getGroupList()) {
            executors.put(userName, true);
        }
        monitor.worked(50);
        for (String userName : getActorList()) {
            executors.put(userName, false);
        }
        monitor.worked(50);
    }

    private List<String> getActorList() throws NamingException {
        String[] ouNames = DesignerPlugin.getPrefString(P_CONNECTION_LDAP_OU).split(";");
        List<String> actors = new ArrayList<String>();
        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_USER_VALUE);
        for (String ou : ouNames) {
            NamingEnumeration<SearchResult> list = LDAPConnector.getInstance().getDirContext().search(ou, attrs);
            while (list.hasMore()) {
                NameClassPair nc = list.next();
                String name = LDAPConnector.getInstance().getDirContext().getAttributes(nc.getName() + "," + ou).get(SAM_ACCOUNT_NAME).get()
                        .toString();
                actors.add(name);
            }
        }
        return actors;
    }

    private List<String> getGroupList() throws NamingException {
        String[] ouNames = DesignerPlugin.getPrefString(P_CONNECTION_LDAP_OU).split(";");
        List<String> groups = new ArrayList<String>();
        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_GROUP_VALUE);
        for (String ou : ouNames) {
            NamingEnumeration<SearchResult> list = LDAPConnector.getInstance().getDirContext().search(ou, attrs);
            while (list.hasMore()) {
                NameClassPair nc = list.next();
                String name = LDAPConnector.getInstance().getDirContext().getAttributes(nc.getName() + "," + ou).get(SAM_ACCOUNT_NAME).get()
                        .toString();
                groups.add(name);
            }
        }
        return groups;
    }

}