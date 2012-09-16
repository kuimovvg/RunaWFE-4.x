package ru.runa.bpm.ui.orgfunctions;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.dialog.ChooseItemDialog;
import ru.runa.bpm.ui.pref.WFEConnectionPreferencePage;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.sync.SyncUIHelper;

public class SwimlaneRelationsElement extends SwimlaneElement {

    private Hyperlink chooseLink;
    private Text relationNameText;
    private Combo combo;

    public SwimlaneRelationsElement() {
        setOrgFunctionDefinitionName("ExecutorByCodeFunction");
    }

    @Override
    public void createGUI(Composite parent) {
        Composite clientArea = createSection(parent, 1);
        SyncUIHelper.createHeader(clientArea, WFEServerRelationsImporter.getInstance(), WFEConnectionPreferencePage.class);

        Composite content1 = new Composite(clientArea, SWT.NONE);
        content1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        content1.setLayout(new GridLayout(2, false));
        relationNameText = new Text(content1, SWT.READ_ONLY | SWT.BORDER);
        relationNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        chooseLink = createLink(content1, Messages.getString("button.choose"));
        chooseLink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                try {
                    ChooseItemDialog dialog = new ChooseItemDialog(Messages.getString("RelationsDialog.Text"), null, true);
                    List<String> relations = WFEServerRelationsImporter.getInstance().loadCachedData();
                    dialog.setItems(relations);
                    dialog.setLabelProvider(new LabelProvider());
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        relationNameText.setText((String) dialog.getSelectedItem());
                        updateSwimlane();
                    }
                } catch (Exception ex) {
                    DesignerLogger.logError("Unable load relations", ex);
                }
            }
        });

        Composite content2 = new Composite(clientArea, SWT.NONE);
        content2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        content2.setLayout(new GridLayout(2, false));
        Label label = new Label(content2, SWT.NONE);
        label.setText(Messages.getString("OrgFunction.ActorCode"));
        combo = new Combo(content2, SWT.READ_ONLY);
        for (String variableName : OrgFunctionsRegistry.getVariableNames(processDefinition, "string")) {
            combo.add(variableName);
        }
        combo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSwimlane();
            }
        });
    }

    private void updateSwimlane() {
        if (currentDefinition == null) {
            currentDefinition = createNew();
        }
        boolean fireEvent = true;
        if (relationNameText.getText().length() > 0) {
            currentDefinition.setRelationName(relationNameText.getText());
        } else {
            fireEvent = false;
        }
        if (combo.getText().length() > 0) {
            currentDefinition.getParameters().get(0).setVariableValue(combo.getText());
        } else {
            fireEvent = false;
        }
        if (fireEvent) {
            fireCompletedEvent(currentDefinition);
        }
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionDefinition definition) {
        super.open(path, swimlaneName, definition);
        if (currentDefinition != null) {
            String relationName = currentDefinition.getRelationName();
            if (relationName != null) {
                relationNameText.setText(relationName);
            }
            String value = "";
            if (currentDefinition != null && currentDefinition.getParameters().size() > 0) {
                value = currentDefinition.getParameters().get(0).getVariableName();
            }
            combo.setText(value);
        }
    }

}
