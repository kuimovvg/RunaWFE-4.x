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
package ru.runa.wfe.bp.commons;

import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.Actor;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.bpm.db.TaskDAO;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.email.EmailSenderImpl;
import ru.runa.wf.email.JbpmHtmlTaskFormParser;
import ru.runa.wf.presentation.WFProfileStrategy;

/**
 * Created on 28.10.2008
 * 
 * @author A. Shautsou
 * @version 1.0 Initial version
 */
public class EmailTaskNotifierActionHandler implements ActionHandler {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(EmailTaskNotifierActionHandler.class);

    private EmailTaskNotifierResources resources;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private TaskDAO taskDAO;

    @Override
    public void setConfiguration(String configuration) {
        resources = new EmailTaskNotifierResources(configuration);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        // if smtp.sendNotification = true
        if (resources.isSmtpSendNotification()) {
            // 1. find all actors
            try {
                Subject subject = DelegateFactory.getInstance().getAuthenticationService().authenticate(resources.getWFUser(),
                        resources.getWFUserPass());
                List<Actor> actors = executorDAO.getAllActors(
                        WFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
                for (Actor actor : actors) {
                    String email = actor.getEmail();
                    // 2. for each actor with not empty email
                    if (email != null && email.length() > 0) {
                        String targetActorId = Long.toString(actor.getId());
                        List<TaskInstance> taskList = taskDAO.findTaskInstances(targetActorId);

                        // 3. checking for state
                        for (TaskInstance taskInstance : taskList) {
                            if (taskInstance.getId() == executionContext.getTaskInstance().getId()) {
                                resources.addTo(email);
                                break;
                            }
                        }

                    }
                }
                // sending email
                Map<String, Object> variables = executionContext.getTaskInstance().getVariables();
                JbpmHtmlTaskFormParser parser = new JbpmHtmlTaskFormParser(subject, null);
                parser.setTask(executionContext.getTaskInstance().getId(), executionContext.getTaskInstance().getTask().getName());
                new EmailSenderImpl().sendMessage(resources, subject, variables, parser.getParsedForm());
            } catch (Exception e) {
                log.error("Can't send email", e);
            }
        }
    }
}
