package ru.runa.wf.web.customtag;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.common.WebResources;
import ru.runa.service.client.DelegateProcessVariableProvider;
import ru.runa.wf.web.tag.HTMLFormConverter;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class FormParser {
    private static final Log log = LogFactory.getLog(FormParser.class);

    private static final Pattern CUSTOM_TAG_PATTERN = Pattern.compile(
            "<customtag\\s+var\\s*=\\s*\"([^\"]+)\"\\s+delegation\\s*=\\s*\"([^\"]+)\"\\s*/>", Pattern.MULTILINE);

    private final User user;
    private final PageContext pageContext;
    private final IVariableProvider variableProvider;
    private final byte[] formBytes;
    private final Long definitionId;
    private final List<String> requiredVariableNames = Lists.newArrayList();

    public FormParser(User user, PageContext pageContext, Interaction interaction, Long definitionId, WfTask task) {
        this.user = user;
        this.pageContext = pageContext;
        if (interaction.hasForm()) {
            formBytes = interaction.getFormData();
        } else {
            formBytes = "No form defined for this task".getBytes();
        }
        if (definitionId != null) {
            this.definitionId = definitionId;
            variableProvider = new MapDelegableVariableProvider(interaction.getDefaultVariableValues(), null);
        } else {
            this.definitionId = task.getDefinitionId();
            variableProvider = new DelegateProcessVariableProvider(user, task.getProcessId());
        }
        if (WebResources.isHighlightRequiredFields()) {
            requiredVariableNames.addAll(interaction.getRequiredVariableNames());
        }
    }

    public byte[] getParsedFormBytes() {
        StringBuilder sb = new StringBuilder(new String(formBytes, Charsets.UTF_8));
        applyTags(sb);
        byte[] formBytes = HTMLFormConverter.changeUrls(pageContext, definitionId, "form.html", sb.toString().getBytes(Charsets.UTF_8));
        return HTMLFormConverter.setInputValues(formBytes, variableProvider, requiredVariableNames);
    }

    private void applyTags(StringBuilder sb) {
        Matcher matcher = CUSTOM_TAG_PATTERN.matcher(sb);
        for (int position = 0; matcher.find(position);) {
            int start = matcher.start();
            int end = matcher.end();
            String varName = matcher.group(1);
            String className = matcher.group(2);
            String replacement;
            try {
                className = BackCompatibilityClassNames.getClassName(className);
                VarTag customTag = (VarTag) ApplicationContextFactory.createAutowiredBean(className);
                replacement = customTag.getHtml(user, varName, variableProvider.getValue(varName), pageContext);
            } catch (Exception e) {
                log.warn("Exception processing vartags", e);
                replacement = "<p class='error'>" + e.getMessage() + "</p>";
            }
            sb.replace(start, end, replacement);
            position = start + replacement.length();
        }
    }

}
