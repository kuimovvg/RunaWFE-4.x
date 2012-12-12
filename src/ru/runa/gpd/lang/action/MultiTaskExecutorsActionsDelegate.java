package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.dialog.CreateVariableDialog;
import ru.runa.wfe.var.format.ArrayListFormat;

import com.google.common.base.Objects;

public class MultiTaskExecutorsActionsDelegate extends BaseModelActionDelegate implements IMenuCreator {
    private String selectedVariable;
    private ProcessDefinition currentDefinition;
    private MultiTaskState currentNode;

    @Override
    public void dispose() {
    }

    @Override
    public Menu getMenu(Control parent) {
        // never called
        return null;
    }

    @Override
    public Menu getMenu(Menu parent) {
        Menu menu = new Menu(parent);
        /**
         * Add listener to re-populate the menu each time it is shown because
         * MenuManager.update(boolean, boolean) doesn't dispose pull-down
         * ActionContribution items for each popup menu.
         */
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                try {
                    Menu m = (Menu) e.widget;
                    MenuItem[] items = m.getItems();
                    for (int i = 0; i < items.length; i++) {
                        items[i].dispose();
                    }
                    fillMenu(m);
                } catch (Exception ex) {
                    PluginLogger.logError(ex);
                }
            }
        });
        return menu;
    }

    @Override
    public void run(IAction action) {
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.isEnabled()) {
            currentNode = getSelection();
            selectedVariable = currentNode.getExecutorsVariableName();
            currentDefinition = currentNode.getProcessDefinition();
            action.setMenuCreator(this);
        }
    }

    /**
     * Fills the menu with applicable launch shortcuts
     * 
     * @param menu
     *            The menu to fill
     */
    protected void fillMenu(Menu menu) {
        for (Variable variable : currentDefinition.getVariablesList()) {
            if (ArrayListFormat.class.getName().equals(variable.getFormat())) {
                Action action = new SetVariableAction();
                action.setText(variable.getName());
                if (Objects.equal(selectedVariable, variable.getName())) {
                    action.setChecked(true);
                }
                ActionContributionItem item = new ActionContributionItem(action);
                item.fill(menu, -1);
            }
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
        CreateVariableDialog dialog = new CreateVariableDialog(currentDefinition, null);
        dialog.setType(ArrayListFormat.class.getName());
        if (dialog.open() == IDialogConstants.OK_ID) {
            Variable variable = new Variable(dialog.getName(), dialog.getType(), dialog.isPublicVisibility(), dialog.getDefaultValue());
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
