package org.jbpm.ui.orgfunctions;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.dialog.ChooseItemDialog;
import org.jbpm.ui.pref.LDAPConnectionPreferencePage;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.sync.SyncUIHelper;

public class SwimlaneADElement extends SwimlaneElement {

    private Hyperlink chooseExecutorLink;
    private Text selectionText;

    public SwimlaneADElement() {
        setOrgFunctionDefinitionName("ExecutorByNameFunction");
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
        chooseExecutorLink = createLink(content, Messages.getString("button.choose"));
        chooseExecutorLink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                try {
                    ChooseItemDialog dialog = new ChooseItemDialog(Messages.getString("ADDialog.Text"), null, true);
                    Map<String, Boolean> executors = LDAPExecutorsImporter.getInstance().loadCachedData();
                    dialog.setItems(new ArrayList<String>(executors.keySet()));
                    dialog.setLabelProvider(new LabelProvider());
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        selectionText.setText((String) dialog.getSelectedItem());
                        OrgFunctionDefinition definition = createNew();
                        definition.getParameters().get(0).setValue(selectionText.getText());
                        fireCompletedEvent(definition);
                    }
                } catch (Exception ex) {
                    DesignerLogger.logError("Unable load cached executors", ex);
                }
            }
        });

    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionDefinition definition) {
        super.open(path, swimlaneName, definition);
        String value = "";
        if (currentDefinition != null && currentDefinition.getParameters().size() > 0) {
            value = currentDefinition.getParameters().get(0).getValue();
        }
        selectionText.setText(value);
    }

}
