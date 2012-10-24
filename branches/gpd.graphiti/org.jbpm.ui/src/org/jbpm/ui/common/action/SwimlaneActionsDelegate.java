package org.jbpm.ui.common.action;

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
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.JpdlVersionRegistry;
import org.jbpm.ui.common.command.EnableReassignmentCommand;
import org.jbpm.ui.common.command.IgnoreSubstitutionCommand;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.StartState;
import org.jbpm.ui.common.model.State;
import org.jbpm.ui.common.model.Swimlane;
import org.jbpm.ui.common.part.graph.FormNodeEditPart;
import org.jbpm.ui.dialog.UpdateSwimlaneNameDialog;
import org.jbpm.ui.editor.DesignerEditor;
import org.jbpm.ui.editor.DesignerGraphicalEditorPart;
import org.jbpm.ui.jpdl3.model.TaskState;
import org.jbpm.ui.resource.Messages;

public class SwimlaneActionsDelegate extends BaseActionDelegate implements IMenuCreator {
    private Swimlane selectedSwimlane;

    private ProcessDefinition currentDefinition;

    private FormNode currentNode;

    public void dispose() {
    }

    public Menu getMenu(Control parent) {
        // never called
        return null;
    }

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
                    DesignerLogger.logError(ex);
                }
            }
        });
        return menu;
    }

    public void run(IAction action) {
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (selectedPart == null) 
            return;
        selectedSwimlane = ((FormNodeEditPart) selectedPart).getModel().getSwimlane();
        currentNode = (FormNode) selectedPart.getModel();
        currentDefinition = (ProcessDefinition) selectedPart.getParent().getModel();
        action.setMenuCreator(this);
        action.setEnabled(true);
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
            Swimlane newSwimlane = JpdlVersionRegistry.getElementTypeDefinition(currentDefinition.getJpdlVersion(), "swimlane").createElement();
            newSwimlane.setParent(currentDefinition);
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
        DesignerEditor editor = ((DesignerGraphicalEditorPart) targetPart).getEditor();
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
            setText(Messages.getString("Swimlane.createSimpleNew"));
        }

        @Override
        public void run() {
            createSwimlane();
        }
    }

    public class ClearSwimlaneAction extends Action {

        public ClearSwimlaneAction() {
            setText(Messages.getString("Swimlane.clear"));
        }

        @Override
        public void run() {
            setSwimlane(null);
        }
    }

    public class GotoSwimlaneAction extends Action {

        public GotoSwimlaneAction() {
            setText(Messages.getString("Swimlane.gotoEdit"));
        }

        @Override
        public void run() {
            editSwimlane();
        }
    }

    public class EnableReassignmentAction extends Action {

        public EnableReassignmentAction() {
            setText(Messages.getString("Swimlane.enableReassignment"));
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
            setText(Messages.getString("property.ignoreSubstitution"));
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
