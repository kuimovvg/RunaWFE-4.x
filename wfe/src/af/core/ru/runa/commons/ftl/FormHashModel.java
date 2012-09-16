package ru.runa.commons.ftl;

import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import freemarker.core.SubjectHolder;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FormHashModel extends SimpleHash {

    private static final long serialVersionUID = 1L;

    private final Subject subject;
    private final PageContext pageContext;
    private final Map<String, Object> variables;

    public FormHashModel(Subject subject, PageContext pageContext, Map<String, Object> variables) {
        super(ObjectWrapper.BEANS_WRAPPER);
        this.subject = subject;
        this.pageContext = pageContext;
        putAll(variables);
        this.variables = variables;
        SubjectHolder.setSubject(subject);
    }

    public PageContext getPageContext() {
        return pageContext;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        TemplateModel model = super.get(key);
        if (model != null) {
            return model;
        }
        try {
            FreemarkerConfiguration conf = FreemarkerConfigurationParser.getConfiguration();
            FreemarkerTag tag = conf.getFreemarkerTag(key);
            if (tag != null) {
                tag.setSubject(subject);
                tag.setPageContext(pageContext);
                tag.setVariables(variables);
                if (pageContext != null) {
                    pageContext.getSession().setAttribute(key, tag);
                }
                return tag;
            }
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
        return null;
    }
}
