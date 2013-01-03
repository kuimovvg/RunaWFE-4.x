package ru.runa.gpd.lang.action;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskConfigHelper;
import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.handler.HandlerRegistry;
import ru.runa.gpd.handler.action.BotConfigBasedProvider;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.ui.dialog.ChooseBotTaskDialog;
import ru.runa.gpd.util.BotTaskContentUtil;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.util.WorkspaceOperations;

public class BotTaskActionsDelegate extends BaseModelActionDelegate implements IMenuCreator {
    private Swimlane selectedSwimlane;
    private ProcessDefinition currentDefinition;
    private TaskState currentNode;
    private IFolder botFolder;

    @Override
    public void dispose() {
    }

    @Override
    public Menu getMenu(Control parent) {
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
        currentNode = getSelection();
        if (currentNode == null) {
            return;
        }
        selectedSwimlane = currentNode.getSwimlane();
        currentDefinition = currentNode.getProcessDefinition();
        if (currentNode instanceof TaskState && selectedSwimlane != null && selectedSwimlane.getDelegationConfiguration() != null) {
            OrgFunctionDefinition definition = OrgFunctionsRegistry.parseSwimlaneConfiguration(selectedSwimlane.getDelegationConfiguration());
            if (definition != null && BotTask.BOT_EXECUTOR_SWIMLANE_NAME.equals(definition.getName())) {
                if (definition.getParameters().size() > 0) {
                    String value = definition.getParameters().get(0).getValue();
                    for (IFolder folder : ProjectFinder.getAllBotFolders()) {
                        if (folder.getName().equals(value)) {
                            this.botFolder = folder;
                        }
                    }
                }
            }
        }
        action.setMenuCreator(this);
        action.setEnabled(true);
    }

    protected void fillMenu(Menu menu) {
        Action action;
        ActionContributionItem item;
        if (currentNode.getBotTask() == null) {
            action = new BindBotTaskWithNodeAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        } else {
            action = new EditBotTaskWithNodeAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
            action = new UnbindBotTaskFromNodeAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
            action = new OpenBotTaskAction();
            item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    public class BindBotTaskWithNodeAction extends Action {
        public BindBotTaskWithNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.bind"));
        }

        @Override
        public void run() {
            BotTask task = chooseBotTask();
            bindFormalVariableWithReal(task);
        }
    }

    public class UnbindBotTaskFromNodeAction extends Action {
        public UnbindBotTaskFromNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.unbind"));
        }

        @Override
        public void run() {
            //delete delegation configuration
            currentNode.setBotTask(null);
            currentDefinition.setDirty(true);
        }
    }

    public class EditBotTaskWithNodeAction extends Action {
        public EditBotTaskWithNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.edit"));
        }

        @Override
        public void run() {
            BotTask task = currentNode.getBotTask();
            bindFormalVariableWithReal(task);
        }
    }

    public class OpenBotTaskAction extends Action {
        public OpenBotTaskAction() {
            setText(MessageFormat.format(Localization.getString("BotTaskActionsDelegate.gotobottask"), currentNode.getBotTask().getName()));
        }

        @Override
        public void run() {
            BotTask task = currentNode.getBotTask();
            for (IFile file : ProjectFinder.getBotTaskFiles(botFolder)) {
                if (task.getName().equals(file.getName())) {
                    WorkspaceOperations.openBotTask(file);
                }
            }
        }
    }

    private BotTask chooseBotTask() {
        BotTask task = null;
        List<String> botTaskNames = new ArrayList<String>();
        for (IFile file : ProjectFinder.getBotTaskFiles(botFolder)) {
            botTaskNames.add(file.getName());
        }
        ChooseBotTaskDialog dialog = new ChooseBotTaskDialog(botTaskNames);
        String botTaskName = dialog.openDialog();
        if (botTaskName != null) {
            for (IFile file : ProjectFinder.getBotTaskFiles(botFolder)) {
                if (botTaskName.equals(file.getName())) {
                    task = BotTaskContentUtil.getBotTaskFromFile(file);
                    task.setDelegationClassName(task.getClazz());
                    task.setProcessDefinition(currentDefinition);
                }
            }
        }
        return task;
    }

    private void bindFormalVariableWithReal(BotTask task) {
        DelegableProvider provider = HandlerRegistry.getProvider(task.getDelegationClassName());
        if (!BotTaskConfigHelper.isTaskConfigurationInPlugin(task.getDelegationClassName())) {
            provider = new BotConfigBasedProvider();
            provider.setBundle(Activator.getDefault().getBundle());
        }
        String newConfig = provider.showConfigurationDialog(task);
        if (newConfig != null) {
            task.setDelegationConfiguration(newConfig);
            currentDefinition.setDirty(true);
        }
        currentNode.setBotTask(task);
    }
}
