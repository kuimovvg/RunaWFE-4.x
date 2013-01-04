package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IObjectActionDelegate;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.IMultiInstancesContainer;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;

// TODO unused
public class MultiInstancesDelegate extends BaseModelDropDownActionDelegate {
    private ISelection selection;
    private ProcessDefinition definition;
    private GraphElement graphElement;

    @Override
    public void run(IAction action) {
        //        IMultiInstancesContainer container = (IMultiInstancesContainer) getSelection();
        //        MultiInstancesDiscriminatorDialog dialog = new MultiInstancesDiscriminatorDialog(container);
        //        if (dialog.open() != Window.CANCEL) {
        //            multiSubprocess.setVariablesList(dialog.getSubprocessVariables());
        //        } TODO not completed
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
        super.selectionChanged(action, selection);
        if (action.isEnabled()) {
            IMultiInstancesContainer container = (IMultiInstancesContainer) getSelection();
            graphElement = getSelection();
            definition = graphElement.getProcessDefinition();
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
        Action action;
        ActionContributionItem item;
        {
            action = new DiscriminatorAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
        if (graphElement instanceof TaskState) {
            action = new TasksModeAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    public class DiscriminatorAction extends Action {
        public DiscriminatorAction() {
            setText(Localization.getString("label.action.executors"));
            MultiTaskExecutorsActionsDelegate delegate = new MultiTaskExecutorsActionsDelegate();
            delegate.selectionChanged(this, selection);
            setMenuCreator(delegate);
        }

        @Override
        public IMenuCreator getMenuCreator() {
            return super.getMenuCreator();
        }

        @Override
        public void run() {
            ((IObjectActionDelegate) getMenuCreator()).selectionChanged(this, selection);
        }
    }

    public class TasksModeAction extends Action {
        public TasksModeAction() {
            setText(Localization.getString("label.action.tasksExecutionMode"));
            setMenuCreator(new MultiTaskExecutionModeDelegate());
        }
    }
}
