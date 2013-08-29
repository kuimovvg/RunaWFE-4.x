package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Menu;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.wfe.lang.TaskExecutionMode;

import com.google.common.base.Objects;

public class MultiTaskExecutionModeDelegate extends BaseModelDropDownActionDelegate {
    private TaskExecutionMode selectedMode;
    private MultiTaskState currentNode;

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.isEnabled()) {
            currentNode = getSelection();
            selectedMode = currentNode.getTaskExecutionMode();
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
        for (TaskExecutionMode mode : TaskExecutionMode.values()) {
            SetModeAction action = new SetModeAction(mode);
            if (Objects.equal(selectedMode, mode)) {
                action.setChecked(true);
            }
            ActionContributionItem item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    public class SetModeAction extends Action {
        private final TaskExecutionMode mode;

        public SetModeAction(TaskExecutionMode mode) {
            this.mode = mode;
            setText(Localization.getString("label.action.tasksExecutionMode." + mode.name()));
        }

        @Override
        public void run() {
            currentNode.setTaskExecutionMode(mode);
        }
    }
}
