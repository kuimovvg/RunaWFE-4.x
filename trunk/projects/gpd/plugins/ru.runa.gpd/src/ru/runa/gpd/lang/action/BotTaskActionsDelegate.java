package ru.runa.gpd.lang.action;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Menu;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskLink;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.ChooseBotTaskDialog;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.WorkspaceOperations;

import com.google.common.collect.Lists;

public class BotTaskActionsDelegate extends BaseModelDropDownActionDelegate {
    private TaskState currentNode;
    private String botName;

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        currentNode = getSelection();
        if (currentNode == null) {
            return;
        }
        this.botName = BotTaskUtils.getBotName(currentNode.getSwimlane());
    }

    @Override
    protected void fillMenu(Menu menu) {
        Action action;
        ActionContributionItem item;
        if (currentNode.getBotTaskLink() == null) {
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
            try {
                String botTaskName = chooseBotTask();
                if (botTaskName != null) {
                    BotTaskLink botTaskLink = new BotTaskLink();
                    botTaskLink.setBotTaskName(botTaskName);
                    linkWithBotTask(botTaskLink);
                }
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

    public class UnbindBotTaskFromNodeAction extends Action {
        public UnbindBotTaskFromNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.unbind"));
        }

        @Override
        public void run() {
            try {
                linkWithBotTask(null);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

    public class EditBotTaskWithNodeAction extends Action {
        public EditBotTaskWithNodeAction() {
            setText(Localization.getString("BotTaskActionsDelegate.edit"));
        }

        @Override
        public void run() {
            try {
                linkWithBotTask(currentNode.getBotTaskLink());
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

    public class OpenBotTaskAction extends Action {
        public OpenBotTaskAction() {
            setText(MessageFormat.format(Localization.getString("BotTaskActionsDelegate.gotobottask"), currentNode.getBotTaskLink().getBotTaskName()));
        }

        @Override
        public void run() {
            String botTaskName = currentNode.getBotTaskLink().getBotTaskName();
            BotTask botTask = BotCache.getBotTask(botName, botTaskName);
            WorkspaceOperations.openBotTask(BotCache.getBotTaskFile(botTask));
        }
    }

    private String chooseBotTask() throws CoreException, IOException {
        List<BotTask> botTasks = BotCache.getBotTasks(botName);
        List<String> botTaskNames = Lists.newArrayList();
        for (BotTask botTask : botTasks) {
            if (botTask.getType() != BotTaskType.SIMPLE) {
                botTaskNames.add(botTask.getName());
            }
        }
        ChooseBotTaskDialog dialog = new ChooseBotTaskDialog(botTaskNames);
        return dialog.openDialog();
    }

    private void linkWithBotTask(BotTaskLink botTaskLink) {
        currentNode.setBotTaskLink(botTaskLink);
        if (botTaskLink != null) {
            BotTaskUtils.editBotTaskLinkConfiguration(currentNode);
        }
    }
}
