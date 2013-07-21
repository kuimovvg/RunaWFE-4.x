package ru.runa.gpd.swimlane;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.settings.LDAPConnectionPreferencePage;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.gpd.wfe.SyncUIHelper;
import ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction;

public class ActiveDirectorySwimlaneElement extends OrgFunctionSwimlaneElement {
    private Hyperlink chooseExecutorLink;
    private Text selectionText;

    public ActiveDirectorySwimlaneElement() {
        super(ExecutorByNameFunction.class.getName());
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionSwimlaneInitializer swimlaneInitializer) {
        super.open(path, swimlaneName, swimlaneInitializer);
        selectionText.setText(getOrgFunctionParameterValue(0));
    }

    @Override
    public void createGUI(Composite parent) {
        Composite clientArea = createSection(parent, 1);
        SyncUIHelper.createHeader(clientArea, LDAPExecutorsImporter.getInstance(), LDAPConnectionPreferencePage.class);
        Composite content = new Composite(clientArea, SWT.NONE);
        content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        content.setLayout(new GridLayout(2, false));
        selectionText = new Text(content, SWT.READ_ONLY | SWT.BORDER);
        selectionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        chooseExecutorLink = createLink(content, Localization.getString("button.choose"));
        chooseExecutorLink.addHyperlinkListener(new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                ChooseItemDialog dialog = new ChooseItemDialog(Localization.getString("WFDialog.Text"), null, true);
                Map<String, Boolean> executors = LDAPExecutorsImporter.getInstance().loadCachedData();
                dialog.setItems(new ArrayList<String>(executors.keySet()));
                if (dialog.open() == IDialogConstants.OK_ID) {
                    selectionText.setText((String) dialog.getSelectedItem());
                    setOrgFunctionParameterValue(0, selectionText.getText());
                    fireCompletedEvent();
                }
            }
        });
    }
}
