package ru.runa.wfe.commons.ftl;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FormHashModel extends SimpleHash {

    private static final long serialVersionUID = 1L;

    private final User user;
    private final IVariableProvider variableProvider;
    private final WebHelper webHelper;

    public FormHashModel(User user, IVariableProvider variableProvider, WebHelper webHelper) {
        super(ObjectWrapper.BEANS_WRAPPER);
        this.user = user;
        this.variableProvider = variableProvider;
        this.webHelper = webHelper;
        if (this.webHelper != null) {
            this.webHelper.removeAllTags();
        }
    }

    public WebHelper getWebHelper() {
        return webHelper;
    }

    public IVariableProvider getVariableProvider() {
        return variableProvider;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        TemplateModel model = super.get(key);
        if (model != null) {
            return model;
        }
        Object variableValue = variableProvider.getValue(key);
        if (variableValue != null) {
            return wrap(variableValue);
        }
        try {
            FreemarkerConfiguration conf = FreemarkerConfiguration.getInstance(getClass());
            FreemarkerTag tag = conf.getTag(key);
            if (tag != null) {
                tag.init(user, webHelper, variableProvider);
                if (webHelper != null && tag instanceof AjaxFreemarkerTag) {
                    webHelper.setTag(key, (AjaxFreemarkerTag) tag);
                }
                return tag;
            }
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
        return null;
    }
}
