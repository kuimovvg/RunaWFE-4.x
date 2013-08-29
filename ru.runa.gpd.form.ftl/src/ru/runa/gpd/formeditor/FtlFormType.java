package ru.runa.gpd.formeditor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import ru.runa.gpd.formeditor.ftl.FreemarkerUtil.MyTemplateExceptionHandler;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil.ValidationHashModel;
import ru.runa.gpd.lang.model.FormNode;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FtlFormType extends BaseHtmlFormType {
    @Override
    protected Map<String, Integer> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setLocalizedLookup(false);
        cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
        String string = new String(formBytes, "UTF-8");
        Template template = new Template("test", new StringReader(string), cfg, "UTF-8");
        StringWriter out = new StringWriter();
        ValidationHashModel validationHashModel = new ValidationHashModel(formNode.getProcessDefinition());
        template.process(validationHashModel, out);
        return validationHashModel.getUsedVariables();
    }
}
