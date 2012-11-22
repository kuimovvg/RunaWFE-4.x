package ru.runa.gpd.lang.action;

import java.util.List;

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
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.gef.command.EnableReassignmentCommand;
import ru.runa.gpd.editor.gef.command.IgnoreSubstitutionCommand;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.State;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.UpdateSwimlaneNameDialog;

public class SwimlaneActionsDelegate extends BaseModelActionDelegate implements IMenuCreator {
    private Swimlane selectedSwimlane;
    private ProcessDefinition currentDefinition;
    private FormNode currentNode;

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
            selectedSwimlane = currentNode.getSwimlane();
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
        List<Swimlane> swimlanes = currentDefinition.getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            Action action = new SetSwimlaneAction();
            action.setText(swimlane.getName());
            if ((selectedSwimlane != null) && (selectedSwimlane.equals(swimlane))) {
                action.setChecked(true);
            }
            ActionContributionItem item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        new MenuItem(menu, SWT.SEPARATOR);
        Action action;
        ActionContributionItem item;
        action = new GotoSwimlaneAction();
        item = new ActionContributionItem(action);
        item.fill(menu, -1);
        if (currentNode instanceof StartState && selectedSwimlane == null) {
            action = new CreateSwimlaneAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        if (currentNode instanceof State && selectedSwimlane != null) {
            action = new EnableReassignmentAction();
            action.setChecked(((State) currentNode).isReassignmentEnabled());
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        if (currentNode instanceof TaskState) {
            action = new IgnoreSubstitutionAction();
            action.setChecked(((TaskState) currentNode).isIgnoreSubstitution());
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        if (selectedSwimlane != null) {
            action = new ClearSwimlaneAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    private void createSwimlane() {
        UpdateSwimlaneNameDialog newSwimlaneDialog = new UpdateSwimlaneNameDialog(currentDefinition, true);
        if (newSwimlaneDialog.open() == IDialogConstants.OK_ID) {
            String swimlaneName = newSwimlaneDialog.getName();
            Swimlane newSwimlane = NodeRegistry.getNodeTypeDefinition(Swimlane.class).createElement(currentDefinition);
            newSwimlane.setName(swimlaneName);
            newSwimlane.setDelegationClassName(Swimlane.DELEGATION_CLASS_NAME);
            currentDefinition.addSwimlane(newSwimlane);
            setSwimlane(swimlaneName);
        }
    }

    private void setSwimlane(String swimlaneName) {
        if (swimlaneName != null) {
            Swimlane swimlane = currentDefinition.getSwimlaneByName(swimlaneName);
            currentNode.setSwimlane(swimlane);
        } else {
            currentNode.setSwimlane(null);
        }
    }

    private void editSwimlane() {
        ProcessEditorBase editor = getActiveDesignerEditor();
        editor.openPage(1);
        if (currentNode.getSwimlane() != null) {
            editor.select(currentNode.getSwimlane());
        }
    }

    public class SetSwimlaneAction extends Action {
        @Override
        public void run() {
            setSwimlane(getText());
        }
    }

    public class CreateSwimlaneAction extends Action {
        public CreateSwimlaneAction() {
            setText(Localization.getString("Swimlane.createSimpleNew"));
        }

        @Override
        public void run() {
            createSwimlane();
        }
    }

    public class ClearSwimlaneAction extends Action {
        public ClearSwimlaneAction() {
            setText(Localization.getString("Swimlane.clear"));
        }

        @Override
        public void run() {
            setSwimlane(null);
        }
    }

    public class GotoSwimlaneAction extends Action {
        public GotoSwimlaneAction() {
            setText(Localization.getString("Swimlane.gotoEdit"));
        }

        @Override
        public void run() {
            editSwimlane();
        }
    }

    public class EnableReassignmentAction extends Action {
        public EnableReassignmentAction() {
            setText(Localization.getString("Swimlane.enableReassignment"));
        }

        @Override
        public void run() {
            EnableReassignmentCommand command = new EnableReassignmentCommand((State) currentNode);
            executeCommand(command);
        }

        @Override
        public void setChecked(boolean checked) {
            super.setChecked(checked);
        }
    }

    public class IgnoreSubstitutionAction extends Action {
        public IgnoreSubstitutionAction() {
            setText(Localization.getString("property.ignoreSubstitution"));
        }

        @Override
        public void run() {
            IgnoreSubstitutionCommand command = new IgnoreSubstitutionCommand((TaskState) currentNode);
            executeCommand(command);
        }

        @Override
        public void setChecked(boolean checked) {
            super.setChecked(checked);
        }
    }
}
