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
package ru.runa.wf.logic.bot;

import java.util.HashMap;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.DefinitionService;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.definition.par.FileDataProvider;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;

/**
 * Created on 04.07.2005
 * 
 */
public class EmailTaskHandler implements TaskHandler {

    private static final Log log = LogFactory.getLog(EmailTaskHandler.class);

    private EmailConfig config;

    @Override
    public void configure(String configuration) {
        configure(configuration.getBytes(Charsets.UTF_8));
    }

    @Override
    public void configure(byte[] configuration) {
        if (!EmailConfigParser.canParse(configuration)) {
            throw new IllegalArgumentException("invalid configuration");
        }
        config = EmailConfigParser.parse(configuration);
    }

    @Override
    public void handle(final Subject subject, IVariableProvider variableProvider, final WfTask wfTask) throws TaskHandlerException {
        try {
            final DefinitionService definitionService = DelegateFactory.getDefinitionService();
            ExecutionService executionService = DelegateFactory.getExecutionService();
            try {
                Interaction interaction = null;
                if (config.isUseMessageFromTaskForm()) {
                    interaction = definitionService.getTaskInteraction(subject, wfTask.getId());
                }
                FileDataProvider fileDataProvider = new FileDataProvider() {

                    @Override
                    public byte[] getFileData(String fileName) {
                        try {
                            return definitionService.getFile(subject, wfTask.getProcessDefinitionId(), fileName);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                EmailUtils.sendTaskMessage(subject, config, interaction, variableProvider, fileDataProvider);
            } catch (Exception e) {
                if (config.isThrowErrorOnFailure()) {
                    throw e;
                }
                log.error("unable to send email", e);
            }
            executionService.completeTask(subject, wfTask.getId(), new HashMap<String, Object>());
            log.info("completed task: " + wfTask);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

}
