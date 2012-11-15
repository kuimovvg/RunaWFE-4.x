package ru.runa.gpd.formeditor;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.VariableAccess;
import ru.runa.gpd.lang.model.FormNode;
import freemarker.Mode;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FtlFormType extends BaseHtmlFormType {
    @Override
    protected Map<String, Integer> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setLocalizedLookup(false);
        cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
        Template template = new Template("test", new StringReader(new String(formBytes, PluginConstants.UTF_ENCODING)), cfg, PluginConstants.UTF_ENCODING);
        StringWriter out = new StringWriter();
        VariablesCollector validationHashModel = new VariablesCollector(formNode.getProcessDefinition().getVariableNames(true));
        template.process(validationHashModel, out);
        return validationHashModel.getUsedVariables();
    }

    public static class MyTemplateExceptionHandler implements TemplateExceptionHandler {
        @Override
        public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
            PluginLogger.logErrorWithoutDialog("FTL form problem found.", te);
        }
    }

    @SuppressWarnings("serial")
    public static class VariablesCollector extends SimpleHash {
        private static final String VAR_VALUE_PLC = "var";
        private final Map<String, Integer> usedVariables = new HashMap<String, Integer>();
        private final List<String> allVariableNames;

        public VariablesCollector(List<String> allVariableNames) {
            Mode.setDesignerMode();
            this.allVariableNames = allVariableNames;
        }

        public Map<String, Integer> getUsedVariables() {
            return usedVariables;
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            // add output variables / read access
            if (allVariableNames.contains(key)) {
                if (!usedVariables.containsKey(key)) {
                    usedVariables.put(key, READ_ACCESS);
                }
                return wrap(VAR_VALUE_PLC);
            }
            if (MethodTag.hasTag(key)) {
                return new ProcessTagMethod(key);
            }
            usedVariables.put(key, DOUBTFUL);
            return wrap("noop");
        }

        private class ProcessTagMethod implements TemplateMethodModel {
            final String tagId;

            public ProcessTagMethod(String tagId) {
                this.tagId = tagId;
            }

            @Override
            public Object exec(List args) throws TemplateModelException {
                MethodTag tag = MethodTag.getTag(tagId);
                int paramsSize = tag.params.size();
                if (paramsSize != args.size()) {
                    // FIXME add error
                    if (args.size() < paramsSize) {
                        paramsSize = args.size();
                    }
                }
                for (int i = 0; i < paramsSize; i++) {
                    String varName = (String) args.get(i);
                    MethodTag.Param param = tag.params.get(i);
                    if (param.variableAccess == VariableAccess.WRITE) {
                        usedVariables.put(varName, WRITE_ACCESS);
                    } else if (param.variableAccess == VariableAccess.READ) {
                        if (!VAR_VALUE_PLC.equals(varName) && !usedVariables.containsKey(varName) && !param.isRichCombo()) {
                            usedVariables.put(varName, READ_ACCESS);
                        }
                    }
                }
                return "noop";
            }
        }
    }
}
