package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.wfe.lang.TaskExecutionMode;

import com.google.common.base.Objects;

public class MultiTaskExecutionModeDelegate extends BaseModelActionDelegate implements IMenuCreator {
    private TaskExecutionMode selectedMode;
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
            selectedMode = currentNode.getTaskExecutionMode();
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
