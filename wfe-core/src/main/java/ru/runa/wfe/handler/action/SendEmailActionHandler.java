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
package ru.runa.wfe.handler.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.auth.SubjectHolder;
import ru.runa.wfe.task.Task;

/**
 * Send email.
 * 
 * @author dofs[197@gmail.com]
 */
public class SendEmailActionHandler implements ActionHandler {
    private static final Log log = LogFactory.getLog(SendEmailActionHandler.class);
    private String configuration;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(ExecutionContext executionContext) throws Exception {
        if (!EmailConfigParser.canParse(configuration)) {
            throw new ConfigurationException("Invalid configuration " + configuration);
        }
        EmailConfig config = EmailConfigParser.parse(configuration);
        try {
            Interaction interaction = null;
            if (config.isUseMessageFromTaskForm()) {
                Task task = executionContext.getTask();
                if (task == null) {
                    throw new ConfigurationException("task is null");
                }
                interaction = executionContext.getProcessDefinition().getInteractionNotNull(task.getName());
            }
            EmailUtils.sendTaskMessage(SubjectHolder.get(), config, interaction, executionContext.getVariableProvider(),
                    executionContext.getProcessDefinition());
        } catch (Exception e) {
            if (config.isThrowErrorOnFailure()) {
                throw e;
            }
            log.error("unable to send email", e);
        }
    }
}
