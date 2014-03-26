package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.swimlane.RelationComposite;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.wfe.user.Group;

public class MultiinstanceComposite extends Composite {
    private final ProcessDefinition processDefinition;
    private final MultiinstanceParameters parameters;
    private CTabFolder tabFolder;

    public MultiinstanceComposite(Composite parent, ProcessDefinition processDefinition, final MultiinstanceParameters parameters, String variableLabelText, String groupLabelText) {
        super(parent, SWT.NONE);
        this.processDefinition = processDefinition;
        this.parameters = parameters;
        setLayout(new GridLayout());
        tabFolder = new CTabFolder(this, SWT.TOP | SWT.BORDER);
        tabFolder.setToolTipText(Localization.getString("Multiinstance.TypeMultiInstance"));
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        {
            Composite composite1 = new Composite(tabFolder, SWT.NONE);
            composite1.setLayout(new GridLayout());
            CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("Multiinstance.tab.variable"));
            tabItem1.setControl(composite1);
            createTabVariable(composite1, variableLabelText);
        }
        {
            Composite composite1 = new Composite(tabFolder, SWT.NONE);
            composite1.setLayout(new GridLayout());
            CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("Multiinstance.tab.group"));
            tabItem1.setControl(composite1);
            createTabGroup(composite1, groupLabelText);
        }
        {
            Composite composite1 = new Composite(tabFolder, SWT.NONE);
            composite1.setLayout(new GridLayout());
            CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("Multiinstance.tab.relation"));
            tabItem1.setControl(composite1);
            createTabRelation(composite1);
        }
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        if (VariableMapping.USAGE_DISCRIMINATOR_VARIABLE.equals(parameters.getDiscriminatorType())) {
            tabFolder.setSelection(0);
            gridData.heightHint = 100;
        } else if (VariableMapping.USAGE_DISCRIMINATOR_GROUP.equals(parameters.getDiscriminatorType())) {
            tabFolder.setSelection(1);
            gridData.heightHint = 100;
        } else if (VariableMapping.USAGE_DISCRIMINATOR_RELATION.equals(parameters.getDiscriminatorType())) {
            tabFolder.setSelection(2);
            gridData.heightHint = 170;
        } else {
            throw new RuntimeException("Unexpected type value = " + parameters.getDiscriminatorType());
        }
        setLayoutData(gridData);
        tabFolder.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                int newPageIndex = tabFolder.indexOf((CTabItem) e.item);
                GridData gridData = (GridData) getLayoutData();
                if (newPageIndex == 0) {
                    parameters.setType(VariableMapping.USAGE_DISCRIMINATOR_VARIABLE);
                    gridData.heightHint = 100;
                } else if (newPageIndex == 1) {
                    parameters.setType(VariableMapping.USAGE_DISCRIMINATOR_GROUP);
                    gridData.heightHint = 100;
                } else if (newPageIndex == 2) {
                    parameters.setType(VariableMapping.USAGE_DISCRIMINATOR_RELATION);
                    gridData.heightHint = 170;
                }
                getParent().layout(true);
            }
        });
    }
    
    private void createTabVariable(Composite parent, String variableLabelText) {
        parent.setLayout(new GridLayout());
        Label variableLabel = new Label(parent, SWT.READ_ONLY);
        variableLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        variableLabel.setText(variableLabelText);
        List<String> variableNames = getDiscriminatorVariableNames();
        final Combo processVariableField = new Combo(parent, SWT.READ_ONLY);
        processVariableField.setItems(variableNames.toArray(new String[variableNames.size()]));
        processVariableField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        processVariableField.setText(parameters.getDiscriminatorVariableName());
        processVariableField.addModifyListener(new LoggingModifyTextAdapter() {
            
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                parameters.setDiscriminatorVariableName(processVariableField.getText());
            }
        });
    }

    private List<String> getDiscriminatorVariableNames() {
        return processDefinition.getVariableNames(false, List.class.getName());
    }

    private void createTabGroup(Composite parent, String groupLabelText) {
        parent.setLayout(new GridLayout());
        Label groupLabel = new Label(parent, SWT.READ_ONLY);
        groupLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupLabel.setText(groupLabelText);
        final List<String> groupVariableNames = getDiscriminatorGroupNames();
        String lastUserInputValue = parameters.isDiscriminatorGroupInputAsText() ? parameters.getDiscriminatorGroup() : null;
        final TypedUserInputCombo groupCombo = new TypedUserInputCombo(parent, lastUserInputValue);
        groupCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupCombo.setShowEmptyValue(false);
        for (String variableName : groupVariableNames) {
            groupCombo.add(variableName);
        }
        groupCombo.setTypeClassName(String.class.getName());
        groupCombo.setText(parameters.getDiscriminatorGroup());
        groupCombo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String selected = groupCombo.getText();
                if (!TypedUserInputCombo.INPUT_VALUE.equals(selected)) {
                    parameters.setDiscriminatorGroupInputAsText(!groupVariableNames.contains(selected));
                    parameters.setDiscriminatorGroup(selected);
                }
            }
        });
    }

    private List<String> getDiscriminatorGroupNames() {
        return processDefinition.getVariableNames(true, String.class.getName(), Group.class.getName());
    }

    private void createTabRelation(Composite parent) {
        parent.setLayout(new GridLayout());
        RelationEditor relationEditor = new RelationEditor(parent);
        relationEditor.init(parameters.getDiscriminatorRelation());
    }

    private class RelationEditor extends RelationComposite {

        public RelationEditor(Composite parent) {
            super(parent, true, processDefinition);
            new InsertVariableTextMenuDetectListener(relationNameText, processDefinition.getVariableNames(false, String.class.getName()));
        }

    }
}
