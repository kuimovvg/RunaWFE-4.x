package org.jbpm.ui.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jbpm.ui.DesignerPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer implements PrefConstants {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
        store.setDefault(P_BPMN_SHOW_SWIMLANE, true);
        store.setDefault(P_DEFAULT_NOTATION, "bpmn");

        store.setDefault(P_FORM_DEFAULT_FCK_EDITOR, FORM_FCK_EDITOR);
        store.setDefault(P_FORM_EXTERNAL_EDITOR_PATH, "");
        store.setDefault(P_FORM_USE_EXTERNAL_EDITOR, false);

        //store.setDefault(P_TASKS_TIMEOUT_ENABLED, false);

        store.setDefault(P_CONNECTION_WFE_PROVIDER_URL, "localhost");
        store.setDefault(P_CONNECTION_LOGIN_MODE, LOGIN_MODE_LOGIN_PASSWORD);
        store.setDefault(P_CONNECTION_LOGIN, "Administrator");
        store.setDefault(P_CONNECTION_PASSWORD, "wf");

        store.setDefault(P_CONNECTION_LDAP_SERVER_URL, "192.168.0.1");

    }

}
