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
package ru.runa.wf.logic.bot.email;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionFormException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.html.ReflectionVarTagFactory;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

import com.google.common.base.Charsets;

/**
 * Created on 24.02.2005
 * 
 */
public class JbpmHtmlTaskFormParser {

    private static final Pattern CUSTOM_TAG_PATTERN = Pattern.compile(
            "<customtag\\s+var\\s*=\\s*\"([^\"]+)\"\\s+delegation\\s*=\\s*\"([^\"]+)\"\\s*/>", Pattern.MULTILINE);

    private final Subject subject;

    private Map<String, Object> variableMap;

    private byte[] formBytes;

    private final PageContext pageContext;

    public JbpmHtmlTaskFormParser(Subject subject, PageContext pageContext) {
        this.subject = subject;
        this.pageContext = pageContext;
    }

    /**
     * Designed for parsing task forms.
     * 
     * @param taskId
     * @param taskName
     * 
     * @throws TaskDoesNotExistException
     * @throws AuthorizationException
     * @throws AuthenticationException
     * @throws ProcessDefinitionFormException
     * @throws ProcessDefinitionDoesNotExistException
     * @throws IOException
     */
    public void setTask(Long taskId, String taskName) throws TaskDoesNotExistException, AuthorizationException, AuthenticationException,
            ProcessDefinitionFormException, IOException, ProcessDefinitionDoesNotExistException {
        DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        formBytes = definitionService.getTaskInteraction(subject, taskId, taskName).getFormDataNotNull();
        variableMap = executionService.getVariables(subject, taskId);
    }

    public String getParsedForm() throws AuthenticationException, WorkflowFormProcessingException, UnsupportedEncodingException {
        if (variableMap == null || formBytes == null) {
            throw new IllegalStateException("Variables or form bytes was not ininitialized");
        }
        StringBuilder sb = new StringBuilder(new String(formBytes, Charsets.UTF_8));
        applyTags(sb);
        return sb.toString();
    }

    private void applyTags(StringBuilder sb) throws WorkflowFormProcessingException, AuthenticationException {
        Matcher matcher = CUSTOM_TAG_PATTERN.matcher(sb);
        for (int position = 0; matcher.find(position);) {
            int start = matcher.start();
            int end = matcher.end();
            String varName = matcher.group(1);
            String className = matcher.group(2);
            VarTag customTag = ReflectionVarTagFactory.create(className);
            // even if the variableMap.get(varName)==null we call varTag because
            // it's up to tag implementor
            // to decide whether it should throw an exception or not
            String replacement = customTag.getHtml(subject, varName, variableMap.get(varName), pageContext);
            sb.replace(start, end, replacement);
            position = start + replacement.length();
        }
    }
}
