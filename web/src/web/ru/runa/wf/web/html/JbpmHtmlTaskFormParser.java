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
package ru.runa.wf.web.html;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionFormException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.tag.HTMLFormConverter;

import com.google.common.base.Charsets;

/**
 * Created on 24.02.2005
 * 
 */
public class JbpmHtmlTaskFormParser {

    private static final Log log = LogFactory.getLog(JbpmHtmlTaskFormParser.class);

    private static final Pattern CUSTOM_TAG_PATTERN = Pattern.compile(
            "<customtag\\s+var\\s*=\\s*\"([^\"]+)\"\\s+delegation\\s*=\\s*\"([^\"]+)\"\\s*/>", Pattern.MULTILINE);

    private final Subject subject;

    private Map<String, Object> variableMap;

    private final PageContext pageContext;
    private final Interaction interaction;

    private Long definitionId;

    public JbpmHtmlTaskFormParser(Subject subject, PageContext pageContext, Interaction interaction) {
        this.subject = subject;
        this.pageContext = pageContext;
        this.interaction = interaction;
    }

    /**
     * Designed for parsing task forms.
     * 
     * @throws TaskDoesNotExistException
     * @throws AuthorizationException
     * @throws AuthenticationException
     * @throws ProcessDefinitionFormException
     * @throws ProcessDefinitionDoesNotExistException
     */
    public void setTask(Long taskId, String taskName) throws TaskDoesNotExistException, AuthorizationException, AuthenticationException,
            ProcessDefinitionFormException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            variableMap = executionService.getVariables(subject, taskId);
            definitionId = executionService.getTask(subject, taskId).getProcessDefinitionId();
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    /**
     * Designed for parsing start forms.
     * 
     * @throws ProcessDefinitionDoesNotExistException
     * @throws AuthorizationException
     * @throws AuthenticationException
     * @throws ProcessDefinitionFormException
     */
    public void setDefinitionId(Long definitionId) throws ProcessDefinitionDoesNotExistException, AuthorizationException, AuthenticationException,
            ProcessDefinitionFormException {
        variableMap = interaction.getDefaultVariableValues();
        this.definitionId = definitionId;
    }

    public byte[] getParsedFormBytes() throws AuthenticationException {
        if (variableMap == null) {
            throw new IllegalStateException("Variables or form bytes was not ininitialized");
        }
        StringBuilder sb = null;
        try {
            sb = new StringBuilder(new String(interaction.getFormData(), Charsets.UTF_8));
            applyTags(sb);
            byte[] formBytes = HTMLFormConverter.changeUrls(pageContext, definitionId, "form.html", sb.toString().getBytes(Charsets.UTF_8));
            return HTMLFormConverter.setInputValues(pageContext, formBytes, variableMap, null);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    private void applyTags(StringBuilder sb) throws AuthenticationException {
        Matcher matcher = CUSTOM_TAG_PATTERN.matcher(sb);
        for (int position = 0; matcher.find(position);) {
            int start = matcher.start();
            int end = matcher.end();
            String varName = matcher.group(1);
            String className = matcher.group(2);
            String replacement;
            try {
                VarTag customTag = ReflectionVarTagFactory.create(className);
                replacement = customTag.getHtml(subject, varName, variableMap.get(varName), pageContext);
            } catch (WorkflowFormProcessingException e) {
                log.warn("WorkflowFormProcessingException", e);
                replacement = "<p class='error'>" + e.getMessage() + "</p>";
            } catch (Exception e) {
                log.warn("Exception processing vartags", e);
                replacement = "<p class='error'>" + e.getMessage() + "</p>";
            }
            sb.replace(start, end, replacement);
            position = start + replacement.length();
        }
    }
}
