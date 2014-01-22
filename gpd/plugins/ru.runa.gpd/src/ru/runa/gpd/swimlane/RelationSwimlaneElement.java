package ru.runa.gpd.swimlane;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.gpd.wfe.WFEServerRelationsImporter;
import ru.runa.wfe.user.Executor;

public class RelationSwimlaneElement extends SwimlaneElement<RelationSwimlaneInitializer> {
    private Hyperlink chooseLink;
    private Text relationNameText;
    private Combo variableCombo;
    private Button inversedButton;

    @Override
    public void open(String path, String swimlaneName, RelationSwimlaneInitializer swimlaneInitializer) {
        super.open(path, swimlaneName, swimlaneInitializer);
        inversedButton.setSelection(getSwimlaneInitializerNotNull().isInversed());
        relationNameText.setText(getSwimlaneInitializerNotNull().getRelationName());
        variableCombo.setText(getSwimlaneInitializerNotNull().getRelationParameterVariableName());
    }

    @Override
    protected RelationSwimlaneInitializer createNewSwimlaneInitializer() {
        return new RelationSwimlaneInitializer();
    }

    @Override
    public void createGUI(Composite parent) {
        SyncUIHelper.createHeader(parent, WFEServerRelationsImporter.getInstance(), WFEConnectionPreferencePage.class, null);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        Label relationNameLabel = new Label(composite, SWT.NONE);
        relationNameLabel.setText(Localization.getString("Relation.Name"));
        relationNameText = new Text(composite, SWT.BORDER);
        relationNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        relationNameText.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                fireCompletedEvent();
            }
        });
        chooseLink = createLink(composite, Localization.getString("button.choose"));
        chooseLink.addHyperlinkListener(new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                ChooseItemDialog dialog = new ChooseItemDialog(Localization.getString("Relations"), null, true);
                List<String> relations = WFEServerRelationsImporter.getInstance().loadCachedData();
                dialog.setItems(relations);
                if (dialog.open() == IDialogConstants.OK_ID) {
                    relationNameText.setText((String) dialog.getSelectedItem());
                    fireCompletedEvent();
                }
            }
        });
        Label parameterLabel = new Label(composite, SWT.NONE);
        parameterLabel.setText(Localization.getString("Relation.Parameter"));
        variableCombo = new Combo(composite, SWT.READ_ONLY);
        for (String variableName : processDefinition.getVariableNames(true, Executor.class.getName(), String.class.getName())) {
            variableCombo.add(variableName);
        }
        variableCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                fireCompletedEvent();
            }
        });
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        inversedButton = new Button(composite, SWT.CHECK);
        inversedButton.setText(Localization.getString("Relation.Inversed"));
        inversedButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                fireCompletedEvent();
            }
        });
        new Label(composite, SWT.NONE);
    }

    @Override
    protected void fireCompletedEvent() {
        boolean fireEvent = true;
        if (relationNameText.getText().length() > 0) {
            getSwimlaneInitializerNotNull().setRelationName(relationNameText.getText());
        } else {
            fireEvent = false;
        }
        if (variableCombo.getText().length() > 0) {
            getSwimlaneInitializerNotNull().setRelationParameterVariableName(variableCombo.getText());
        } else {
            fireEvent = false;
        }
        getSwimlaneInitializerNotNull().setInversed(inversedButton.getSelection());
        if (fireEvent) {
            super.fireCompletedEvent();
        }
    }
}
