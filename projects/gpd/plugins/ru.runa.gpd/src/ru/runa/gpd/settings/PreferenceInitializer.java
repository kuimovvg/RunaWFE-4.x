package ru.runa.gpd.settings;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ru.runa.gpd.Activator;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.wfe.WFEServerConnectorRegistry;
import ru.runa.wfe.commons.SystemProperties;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer implements PrefConstants {
    private String DEFAULT_CONNECTOR_ID = "jboss7.ws";
    
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(P_BPMN_SHOW_SWIMLANE, true);
        store.setDefault(P_DEFAULT_LANGUAGE, Language.BPMN.toString());
        store.setDefault(P_FORM_DEFAULT_FCK_EDITOR, FORM_FCK_EDITOR);
        store.setDefault(P_FORM_EXTERNAL_EDITOR_PATH, "");
        store.setDefault(P_FORM_USE_EXTERNAL_EDITOR, false);
        //store.setDefault(P_TASKS_TIMEOUT_ENABLED, false);
        store.setDefault(P_WFE_CONNECTION_TYPE, DEFAULT_CONNECTOR_ID);
        store.setDefault(P_WFE_CONNECTION_HOST, "localhost");
        store.setDefault(P_WFE_CONNECTION_PORT, WFEServerConnectorRegistry.getEntryNotNull(DEFAULT_CONNECTOR_ID).defaultPort);
        store.setDefault(P_WFE_CONNECTION_VERSION, SystemProperties.getVersion());
        store.setDefault(P_WFE_CONNECTION_LOGIN_MODE, LOGIN_MODE_LOGIN_PASSWORD);
        store.setDefault(P_WFE_CONNECTION_LOGIN, "Administrator");
        store.setDefault(P_WFE_CONNECTION_PASSWORD, "wf");
        store.setDefault(P_LDAP_CONNECTION_PROVIDER_URL, "ldap://192.168.0.1/dc=domain,dc=com");
        store.setDefault(P_DATE_FORMAT_PATTERN, "dd.MM.yyyy");
    }
}