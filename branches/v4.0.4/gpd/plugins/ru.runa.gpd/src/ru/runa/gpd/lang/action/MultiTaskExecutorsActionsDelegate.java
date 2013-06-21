package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.dialog.UpdateVariableDialog;
import ru.runa.wfe.var.format.ListFormat;

import com.google.common.base.Objects;

public class MultiTaskExecutorsActionsDelegate extends BaseModelDropDownActionDelegate {
    private String selectedVariable;
    private ProcessDefinition currentDefinition;
    private MultiTaskState currentNode;

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.isEnabled()) {
            currentNode = getSelection();
            selectedVariable = currentNode.getExecutorsVariableName();
            currentDefinition = currentNode.getProcessDefinition();
        }
    }

    /**
     * Fills the menu with applicable launch shortcuts
     * 
     * @param menu
     *            The menu to fill
     */
    @Override
    protected void fillMenu(Menu menu) {
        for (Variable variable : currentDefinition.getVariables(false, ListFormat.class.getName())) {
            Action action = new SetVariableAction();
            action.setText(variable.getName());
            if (Objects.equal(selectedVariable, variable.getName())) {
                action.setChecked(true);
            }
            ActionContributionItem item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        new MenuItem(menu, SWT.SEPARATOR);
        Action action;
        ActionContributionItem item;
        {
            action = new CreateVariableAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        if (selectedVariable != null) {
            action = new ClearVariableAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    private void createVariable() {
        UpdateVariableDialog dialog = new UpdateVariableDialog(currentDefinition, null);
        dialog.setTypeByFormat(ListFormat.class.getName());
        if (dialog.open() == IDialogConstants.OK_ID) {
            Variable variable = new Variable(dialog.getName(), dialog.getTypeName(), dialog.isPublicVisibility(), dialog.getDefaultValue());
            currentDefinition.addVariable(variable);
            setVariableName(variable.getName());
        }
    }

    private void setVariableName(String variableName) {
        currentNode.setExecutorsVariableName(variableName);
    }

    public class SetVariableAction extends Action {
        @Override
        public void run() {
            setVariableName(getText());
        }
    }

    public class CreateVariableAction extends Action {
        public CreateVariableAction() {
            setText(Localization.getString("label.action.createExecutorsVariable"));
        }

        @Override
        public void run() {
            createVariable();
        }
    }

    public class ClearVariableAction extends Action {
        public ClearVariableAction() {
            setText(Localization.getString("label.action.clearExecutors"));
        }

        @Override
        public void run() {
            setVariableName(null);
        }
    }
}
