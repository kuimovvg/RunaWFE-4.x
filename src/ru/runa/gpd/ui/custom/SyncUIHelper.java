package ru.runa.gpd.ui.custom;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.wfe.ConnectorCallback;
import ru.runa.gpd.wfe.DataImporter;
import ru.runa.wfe.commons.ClassLoaderUtil;

public class SyncUIHelper {

    private static HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

    public static Composite createHeader(Composite parent, DataImporter importer, Class<? extends IPreferencePage> pageClass, ConnectorCallback callback) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, true));
        createConnectionSettingsLink(composite, pageClass);
        createSynchronizeLink(composite, importer, callback);
        return composite;
    }

    public static void createConnectionSettingsLink(final Composite parent, final Class<? extends IPreferencePage> pageClass) {
        Hyperlink editSettingsLink = createLink(parent, Localization.getString("button.ConnectionSettings"));
        editSettingsLink.addHyperlinkListener(new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                openConnectionSettingsDialog(pageClass);
            }

        });
    }

    public static void openConnectionSettingsDialog(final Class<? extends IPreferencePage> pageClass) {
        IPreferencePage page = ClassLoaderUtil.instantiate(pageClass);
        PreferenceManager preferenceManager = new PreferenceManager();
        IPreferenceNode node = new PreferenceNode("1", page);
        preferenceManager.addToRoot(node);
        PreferenceDialog dialog = new PreferenceDialog(Display.getCurrent().getActiveShell(), preferenceManager);
        dialog.create();
        dialog.setMessage(page.getTitle());
        dialog.open();
    }

    public static void createSynchronizeLink(Composite parent, final DataImporter importer, final ConnectorCallback callback) {
        final Hyperlink syncItemsLink = createLink(parent, Localization.getString("button.Synchronize"));
        syncItemsLink.addHyperlinkListener(new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                try {
                    importer.synchronize();
                    if (callback != null) {
                        callback.onSynchronizationCompleted();
                    }
                } catch (Exception ex) {
                    if (callback != null) {
                        callback.onSynchronizationFailed(ex);
                    } else {
                        Dialogs.error(Localization.getString("error.Synchronize"), ex);
                    }
                }
            }
        });
        syncItemsLink.setEnabled(importer.isConfigured());
    }

    private static Hyperlink createLink(Composite parent, String msg) {
        Hyperlink link = new Hyperlink(parent, SWT.NONE);
        link.setText(msg);
        hyperlinkGroup.add(link);
        return link;
    }

}
