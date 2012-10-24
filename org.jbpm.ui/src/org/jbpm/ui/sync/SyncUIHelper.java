package org.jbpm.ui.sync;

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
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.jbpm.ui.dialog.ErrorDialog;
import org.jbpm.ui.resource.Messages;

public class SyncUIHelper {

    private static HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

    public static Composite createHeader(final Composite parent, final DataImporter importer, final Class<? extends IPreferencePage> pageClass) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, true));
        createConnectionSettingsLink(composite, pageClass);
        createSynchronizeLink(composite, importer);
        return composite;
    }

    public static void createConnectionSettingsLink(final Composite parent, final Class<? extends IPreferencePage> pageClass) {
        Hyperlink editSettingsLink = createLink(parent, Messages.getString("button.ConnectionSettings"));
        editSettingsLink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                openConnectionSettingsDialog(pageClass);
            }

        });
    }

    public static void openConnectionSettingsDialog(final Class<? extends IPreferencePage> pageClass) {
        try {
            IPreferencePage page = pageClass.newInstance();
            PreferenceManager preferenceManager = new PreferenceManager();
            IPreferenceNode node = new PreferenceNode("1", page);
            preferenceManager.addToRoot(node);
            PreferenceDialog dialog = new PreferenceDialog(Display.getCurrent().getActiveShell(), preferenceManager);
            dialog.create();
            dialog.setMessage(page.getTitle());
            dialog.open();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createSynchronizeLink(Composite parent, final DataImporter importer) {
        final Hyperlink syncItemsLink = createLink(parent, Messages.getString("button.Synchronize"));
        syncItemsLink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                try {
                    importer.synchronize();
                } catch (Exception ex) {
                    ErrorDialog.open(Messages.getString("error.Synchronize"), ex);
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
