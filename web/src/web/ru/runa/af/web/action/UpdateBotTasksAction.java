/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Bot;
import ru.runa.af.BotTask;
import ru.runa.af.service.BotsService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.BotTasksForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Sets;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/update_bot_tasks" name="botTasksForm" validate="true" input = "/WEB-INF/wf/bot.jsp"
 */
public class UpdateBotTasksAction extends Action {
    public static final String UPDATE_BOT_TASKS_ACTION_PATH = "/update_bot_tasks";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BotTasksForm tasksForm = (BotTasksForm) form;
        try {
            BotsService botsService = DelegateFactory.getInstance().getBotsService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            Bot bot = new Bot();
            bot.setId(tasksForm.getId());
            bot = botsService.getBot(subject, bot);
            List<BotTask> tasks = botsService.getBotTaskList(subject, bot);

            Set<Long> checkedIdSet = Sets.newHashSet(tasksForm.getIds());

            List<BotTask> removedTasks = new ArrayList<BotTask>(tasks.size() - checkedIdSet.size());
            List<BotTask> updatedTasks = new ArrayList<BotTask>(checkedIdSet.size());
            for (BotTask task : tasks) {
                if (!checkedIdSet.contains(task.getId())) {
                    removedTasks.add(task);
                } else {
                    BotTasksForm.BotTaskForm updatedTask = tasksForm.getBotTaskForm(task.getId());
                    task.setName(updatedTask.getName());
                    task.setClazz(updatedTask.getHandler());
                    task.setConfiguration(updatedTask.getConfigFile().getFileData());
                    updatedTasks.add(task);
                }
            }

            for (BotTask task : removedTasks) {
                botsService.remove(subject, task);
            }

            for (BotTask task : updatedTasks) {
                botsService.update(subject, task);
            }

        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return new ActionForward("/bot.do?botID=" + tasksForm.getId());
    }
}
