package org.jbpm.ui.orgfunctions;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jbpm.ui.resource.Messages;

public class SwimlaneGeneralElement extends SwimlaneElement {
    private Control[] userInputControls;

    @Override
    public String getDisplayName() {
        return createNew().getName();
    }

    @Override
    public void createGUI(Composite parent) {
        Composite clientArea = createSection(parent, 2);

        if (currentDefinition == null) {
            currentDefinition = createNew();
        }
        int paramsSize = currentDefinition.getParameters().size();
        userInputControls = new Control[paramsSize];
        for (int i = 0; i < paramsSize; i++) {
            final OrgFunctionParameter parameter = currentDefinition.getParameters().get(i);
            String message = Messages.getString(parameter.getName()) + " *:";
            Label label = new Label(clientArea, SWT.NONE);
            label.setText(message);
            label.setLayoutData(createLayoutData(false));

            final Combo combo = new Combo(clientArea, SWT.READ_ONLY);
            combo.setVisibleItemCount(10);
            Set<String> variableNames = OrgFunctionsRegistry.getVariableNames(processDefinition, parameter.getType());
            for (String varName : variableNames) {
                if (!varName.equals(swimlaneName)) {
                    combo.add(varName);
                }
            }
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String value = combo.getItem(combo.getSelectionIndex());
                    updateSwimlane(parameter.getName(), value);
                }
            });
            combo.setLayoutData(createLayoutData(true));
            userInputControls[i] = combo;
        }
    }

    private GridData createLayoutData(boolean fillGrab) {
        return new GridData(fillGrab ? GridData.FILL_HORIZONTAL : GridData.CENTER);
    }

    private void updateSwimlane(String paramName, String value) {
        currentDefinition.getParameter(paramName).setVariableValue(value);
        boolean fireEvent = true;
        for (OrgFunctionParameter parameter : currentDefinition.getParameters()) {
            if (parameter.getValue().length() == 0)
                fireEvent = false;
        }
        if (fireEvent) {
            fireCompletedEvent(currentDefinition);
        }
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionDefinition definition) {
        super.open(path, swimlaneName, definition);
        if (currentDefinition == null) {
            currentDefinition = createNew();
        }
        for (int i = 0; i < userInputControls.length; i++) {
            String variableName = "";
            if (currentDefinition.getParameters().get(i).isUseVariable()) {
                variableName = currentDefinition.getParameters().get(i).getVariableName();
            }
            if (userInputControls[i] instanceof Text) {
                ((Text) userInputControls[i]).setText(variableName);
            }
            if (userInputControls[i] instanceof Combo) {
                ((Combo) userInputControls[i]).setText(variableName);
            }
        }
    }

}
