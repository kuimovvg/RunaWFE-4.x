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
package ru.runa.wf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.ConfigurationException;
import ru.runa.af.authenticaion.SubjectHolder;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.commons.email.EmailConfig;
import ru.runa.commons.email.EmailConfig.Attachment;
import ru.runa.commons.email.EmailConfigParser;
import ru.runa.commons.email.EmailUtils;
import ru.runa.commons.ftl.FormHashModel;
import ru.runa.commons.ftl.FreemarkerProcessor;
import ru.runa.commons.html.HTMLUtils;

import com.google.common.base.Charsets;

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
        Map<String, Object> variables = executionContext.getContextInstance().getVariables();
        EmailConfig config = EmailConfigParser.parse(configuration);
        config.applySubstitutions(variables);
        try {
            byte[] formBytes;
            if (config.isUseMessageFromTaskForm()) {
                Task task = executionContext.getTask();
                if (task == null) {
                    throw new ConfigurationException("task is null, check action handler attached to task");
                }
                // TODO get form file from metadata
                String fileName = task.getName() + ".ftl";
                formBytes = getProcessFile(executionContext, fileName);
                if (formBytes == null) {
                    log.warn("No file data found at " + fileName);
                    formBytes = new byte[0];
                }
            } else {
                formBytes = config.getMessage().getBytes(Charsets.UTF_8);
            }
            FormHashModel model = new FormHashModel(SubjectHolder.get(), null, variables);
            String formMessage = FreemarkerProcessor.process(formBytes, model);

            Map<String, String> replacements = new HashMap<String, String>();

            List<Attachment> attachments = new ArrayList<Attachment>();
            List<String> images = HTMLUtils.findImages(formBytes);
            for (String image : images) {
                Attachment attachment = new Attachment();
                attachment.fileName = image;
                attachment.content = getProcessFile(executionContext, attachment.fileName);
                attachment.inlined = true;
                attachments.add(attachment);
                replacements.put(attachment.fileName, "cid:" + attachment.fileName);
            }
            for (String variableName : config.getAttachments()) {
                FileVariable fileVariable = (FileVariable) variables.get(variableName);
                Attachment attachment = new Attachment();
                attachment.fileName = fileVariable.getName();
                attachment.content = fileVariable.getData();
                attachments.add(attachment);
            }
            for (String repl : replacements.keySet()) {
                formMessage = formMessage.replaceAll(repl, replacements.get(repl));
            }
            config.setMessage(formMessage);
            EmailUtils.sendMessage(config, attachments);
        } catch (Exception e) {
            if (config.isThrowErrorOnFailure()) {
                throw e;
            }
            log.error("unable to send email", e);
        }
    }

    private byte[] getProcessFile(ExecutionContext executionContext, String name) {
        return executionContext.getProcessDefinition().getFileBytes(name);
    }
}
