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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Created on 04.07.2005
 * 
 */
public class EmailTaskHandler extends TaskHandlerBase {
    private static final Log log = LogFactory.getLog(EmailTaskHandler.class);

    protected EmailConfig config;

    @Override
    public void setConfiguration(String configuration) {
        if (!EmailConfigParser.canParse(configuration)) {
            throw new InternalApplicationException("Format of email configuration has been changed in 4.x");
        }
        config = EmailConfigParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(final User user, IVariableProvider variableProvider, final WfTask task) throws Exception {
        try {
            Interaction interaction = null;
            if (config.isUseMessageFromTaskForm()) {
                interaction = Delegates.getDefinitionService().getTaskInteraction(user, task.getId());
            }
            // FileDataProvider fileDataProvider = new FileDataProvider() {
            //
            // @Override
            // public byte[] getFileData(String fileName) {
            // try {
            // return
            // Delegates.getDefinitionService().getProcessDefinitionFile(user,
            // task.getDefinitionId(), fileName);
            // } catch (Exception e) {
            // throw new RuntimeException(e);
            // }
            // }
            // };
            EmailUtils.sendTaskMessage(user, config, interaction, variableProvider);
        } catch (Exception e) {
            if (config.isThrowErrorOnFailure()) {
                throw e;
            }
            log.error("unable to send email", e);
        }
        return null;
    }

}
