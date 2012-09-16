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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.commons.email.EmailConfig;
import ru.runa.commons.email.EmailConfig.Attachment;
import ru.runa.commons.email.EmailConfigParser;
import ru.runa.commons.email.EmailResources;
import ru.runa.commons.email.EmailUtils;
import ru.runa.commons.ftl.FormHashModel;
import ru.runa.commons.ftl.FreemarkerProcessor;
import ru.runa.commons.html.HTMLUtils;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.FileVariable;
import ru.runa.wf.TaskStub;
import ru.runa.wf.email.EmailSenderImpl;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.logic.bot.email.JbpmHtmlTaskFormParser;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;

import com.google.common.base.Charsets;

/**
 * Created on 04.07.2005
 * 
 */
public class EmailTaskHandler implements TaskHandler {

    private static final Log log = LogFactory.getLog(EmailTaskHandler.class);

    private EmailResources resources;
    private EmailConfig config;

    public void configure(String configuration) {
        resources = new EmailResources(configuration);
    }

    public void configure(byte[] configuration) throws TaskHandlerException {
        if (EmailConfigParser.canParse(configuration)) {
            config = EmailConfigParser.parse(configuration);
        }
        resources = new EmailResources(configuration);
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, Object> variables = executionService.getVariables(subject, taskStub.getId());

            if (config != null) {
                config.applySubstitutions(variables);
                try {
                    byte[] formBytes;
                    if (config.isUseMessageFromTaskForm()) {
                        Interaction interaction = definitionService.getTaskInteraction(subject, taskStub.getId(), taskStub.getName());
                        if (interaction.hasFile()) {
                            formBytes = interaction.getFormData();
                        } else {
                            log.warn("No form file found for " + taskStub.getName());
                            formBytes = new byte[0];
                        }
                    } else {
                        formBytes = config.getMessage().getBytes(Charsets.UTF_8);
                    }
                    FormHashModel model = new FormHashModel(subject, null, variables);
                    String formMessage = FreemarkerProcessor.process(formBytes, model);

                    Map<String, String> replacements = new HashMap<String, String>();

                    List<Attachment> attachments = new ArrayList<Attachment>();
                    List<String> images = HTMLUtils.findImages(formBytes);
                    for (String image : images) {
                        Attachment attachment = new Attachment();
                        attachment.fileName = image;
                        attachment.content = definitionService.getFile(subject, taskStub.getProcessDefinitionId(), attachment.fileName);
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
            } else {
                JbpmHtmlTaskFormParser parser = new JbpmHtmlTaskFormParser(subject, null);
                parser.setTask(taskStub.getId(), taskStub.getName());
                new EmailSenderImpl().sendMessage(resources, subject, variables, parser.getParsedForm());
            }

            executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(),
                    new HashMap<String, Object>());
            log.info("completed task: " + taskStub);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

}
