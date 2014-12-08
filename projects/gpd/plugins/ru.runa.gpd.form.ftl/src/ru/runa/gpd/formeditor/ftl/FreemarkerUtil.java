package ru.runa.gpd.formeditor.ftl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.formeditor.BaseHtmlFormType;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.formeditor.ftl.MethodTag.VariableAccess;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import freemarker.Mode;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@SuppressWarnings({ "serial", "rawtypes" })
public class FreemarkerUtil {
    public static final String IMAGE_DIR = "/editor/plugins/FreemarkerTags/im/";
    public static final String PARAMETERS_DELIM = "|";

    private static String METHOD_ELEMENT_NAME() {
        return WebServerUtils.useCKEditor() ? "ftl_element" : "img";
    }

    private static String OUTPUT_ELEMENT_NAME() {
        return WebServerUtils.useCKEditor() ? "ftl_element_output" : "img";
    }

    private static final String ATTR_FTL_TAG_FORMAT = "ftltagformat";
    private static final String ATTR_FTL_TAG_PARAMS = "ftltagparams";
    private static final String ATTR_FTL_TAG_NAME = "ftltagname";
    private static final String ATTR_STYLE = "style";

    public static String[] splitTagParameters(String tagParams) {
        if (Strings.isNullOrEmpty(tagParams)) {
            return new String[0];
        }
        return tagParams.split("\\" + PARAMETERS_DELIM);
    }

    public static String transformFromHtml(String html, Map<String, Variable> variables) throws Exception {
        Document document = BaseHtmlFormType.getDocument(new ByteArrayInputStream(html.getBytes(Charsets.UTF_8)));
        NodeList tagElements = document.getElementsByTagName(METHOD_ELEMENT_NAME());
        int len = tagElements.getLength();
        int idx = 0;
        for (int i = 0; i < len; i++) {
            Node domNode = tagElements.item(idx);
            if (domNode == null) {
                if (EditorsPlugin.DEBUG) {
                    PluginLogger.logErrorWithoutDialog("Null tag element at " + i + " in " + html);
                }
                continue;
            }
            String tagName = getAttrValue(domNode, ATTR_FTL_TAG_NAME);
            if (tagName == null) {
                continue;
            }
            String tagParams = getAttrValue(domNode, ATTR_FTL_TAG_PARAMS);
            if (tagParams == null) {
                // no params
                tagParams = "";
            }
            // Method
            StringBuffer ftlTag = new StringBuffer();
            ftlTag.append("${").append(tagName);
            ftlTag.append("(");
            if (tagParams.length() > 0) {
                String[] params = splitTagParameters(tagParams);
                for (int j = 0; j < params.length; j++) {
                    if (j != 0) {
                        ftlTag.append(", ");
                    }
                    boolean surroundWithBrackets = true;
                    try {
                        if (MethodTag.hasTag(tagName)) {
                            MethodTag tag = MethodTag.getTagNotNull(tagName);
                            if (tag.params.size() > j) {
                                Param param = tag.params.get(j);
                                if (param.isVarCombo() || (param.isRichCombo() && variables.containsKey(params[j]))) {
                                    surroundWithBrackets = false;
                                }
                            }
                        }
                    } catch (Exception e) {
                        PluginLogger.logErrorWithoutDialog("FTL tag problem found for " + tagName + "(" + j + "): '" + params[j] + "'", e);
                    }
                    if (!MethodTag.hasTag(tagName)) {
                        throw new NullPointerException(tagName);
                    }
                    if (surroundWithBrackets) {
                        ftlTag.append("\"");
                    }
                    ftlTag.append(params[j]);
                    if (surroundWithBrackets) {
                        ftlTag.append("\"");
                    }
                }
            }
            ftlTag.append(")");
            ftlTag.append("}");
            Text ftlText = document.createTextNode(ftlTag.toString());
            domNode.getParentNode().replaceChild(ftlText, domNode);
        }
        tagElements = document.getElementsByTagName(OUTPUT_ELEMENT_NAME());
        len = tagElements.getLength();
        idx = 0;
        for (int i = 0; i < len; i++) {
            Node domNode = tagElements.item(idx);
            String tagName = getAttrValue(domNode, ATTR_FTL_TAG_NAME);
            if (tagName == null) {
                continue;
            }
            String tagFormat = getAttrValue(domNode, ATTR_FTL_TAG_FORMAT);
            if (tagFormat != null) {
                // Output value
                String ftlTag = "${" + tagName;
                if (!"-".equals(tagFormat)) {
                    ftlTag += "?";
                    ftlTag += tagFormat;
                }
                ftlTag += "}";
                Text ftlText = document.createTextNode(ftlTag);
                domNode.getParentNode().replaceChild(ftlText, domNode);
            } else {
                idx++;
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeHtml(document, os);
        return new String(os.toByteArray(), Charsets.UTF_8);
    }

    private static void writeHtml(Document document, OutputStream os) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, Charsets.UTF_8.name());
        transformer.transform(new DOMSource(document), new StreamResult(os));
    }

    private static String getAttrValue(Node domNode, String attr) {
        Node attrNode = domNode.getAttributes().getNamedItem(attr);
        if (attrNode == null) {
            return null;
        }
        return attrNode.getNodeValue();
    }

    public static String transformToHtml(Map<String, Variable> variables, String ftlText) throws Exception {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setLocalizedLookup(false);
        cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
        Template template = new Template("test", new StringReader(ftlText), cfg, Charsets.UTF_8.name());
        StringWriter out = new StringWriter();
        template.process(new EditorHashModel(variables), out);
        out.flush();
        return out.toString();
    }

    private static class VariableTypeSupportHashModel extends SimpleHash {
        protected static final String VAR_VALUE_PLC = "var";
        protected boolean stageRenderingParams = false;

        protected TemplateModel getTemplateModel(Variable variable) throws TemplateModelException {
            Map<String, String> properties = new HashMap<String, String>();
            String javaClassName = variable.getJavaClassName();
            if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.user.Executor", javaClassName)) {
                addPropertyDescriptor(properties, "id", variable.getName());
                addPropertyDescriptor(properties, "name", variable.getName());
                addPropertyDescriptor(properties, "fullName", variable.getName());
                addPropertyDescriptor(properties, "description", variable.getName());
                addPropertyDescriptor(properties, "version", variable.getName());
            }
            if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.user.Actor", javaClassName)) {
                addPropertyDescriptor(properties, "active", variable.getName());
                addPropertyDescriptor(properties, "code", variable.getName());
                addPropertyDescriptor(properties, "email", variable.getName());
                addPropertyDescriptor(properties, "phone", variable.getName());
            }
            if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.user.Group", javaClassName)) {
                addPropertyDescriptor(properties, "ldapGroupName", variable.getName());
            }
            if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.var.FileVariable", javaClassName)) {
                addPropertyDescriptor(properties, "name", variable.getName());
                addPropertyDescriptor(properties, "contentType", variable.getName());
                addPropertyDescriptor(properties, "dataLength", variable.getName());
            }
            if (VariableFormatRegistry.isAssignableFrom(Date.class.getName(), javaClassName)) {
                addPropertyDescriptor(properties, "date", variable.getName());
                addPropertyDescriptor(properties, "day", variable.getName());
                addPropertyDescriptor(properties, "hours", variable.getName());
                addPropertyDescriptor(properties, "minutes", variable.getName());
                addPropertyDescriptor(properties, "month", variable.getName());
                addPropertyDescriptor(properties, "seconds", variable.getName());
                addPropertyDescriptor(properties, "time", variable.getName());
                addPropertyDescriptor(properties, "timezoneOffset", variable.getName());
                addPropertyDescriptor(properties, "year", variable.getName());
            }
            if (properties.size() == 0) {
                return new SimpleScalar("${" + variable.getName() + "}");
            }
            return new SimpleHash(properties);
        }

        private void addPropertyDescriptor(Map<String, String> properties, String name, String variableName) {
            properties.put(name, "${" + variableName + "." + name + "}");
        }

    }

    public static class EditorHashModel extends VariableTypeSupportHashModel {
        private final Map<String, Variable> variables;

        public EditorHashModel(Map<String, Variable> variables) {
            Mode.setDesignerMode();
            this.variables = variables;
        }

        private TemplateModel wrapParameter(String prefix, Variable variable) {
            if (prefix != null) {
                prefix += "." + variable.getName();
            } else {
                prefix = variable.getName();
            }
            if (variable.getUserType() != null) {
                Map<String, TemplateModel> properties = new HashMap<String, TemplateModel>();
                for (Variable attribute : variable.getUserType().getAttributes()) {
                    properties.put(attribute.getName(), wrapParameter(prefix, attribute));
                }
                return new SimpleHash(properties);
            }
            return new SimpleScalar(prefix);
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            if (stageRenderingParams) {
                Variable variable = variables.get(key);
                if (variable != null) {
                    return wrapParameter(null, variable);
                }
                return new SimpleScalar(key);
            }
            // output variables
            if (variables.containsKey(key)) {
                return getTemplateModel(variables.get(key));
            }
            stageRenderingParams = true;
            return new EditorMethodModel(key);
        }

        public class EditorMethodModel implements TemplateMethodModel {
            private String name;

            public EditorMethodModel(String name) {
                this.name = name;
            }

            @Override
            public Object exec(List args) throws TemplateModelException {
                stageRenderingParams = false;
                StringBuffer buffer = new StringBuffer("<").append(METHOD_ELEMENT_NAME()).append(" ");
                buffer.append(ATTR_FTL_TAG_NAME).append("=\"").append(name).append("\" ");
                StringBuffer params = new StringBuffer();
                for (int i = 0; i < args.size(); i++) {
                    if (i != 0) {
                        params.append(PARAMETERS_DELIM);
                    }
                    params.append(args.get(i).toString());
                }
                buffer.append(ATTR_FTL_TAG_PARAMS).append("=\"").append(params).append("\" ");
                if (!WebServerUtils.useCKEditor()) {
                    String url = "http://localhost:48780/editor/FreemarkerTags.java?method=GetTagImage&tagName=" + name + "&tagParams=" + params;
                    buffer.append("src=\"").append(url).append("\" ");
                    buffer.append(ATTR_STYLE).append("=\"margin: 3px;\" ");
                }
                buffer.append("/>");
                return buffer.toString();
            }
        }
    }

    public static class MyTemplateExceptionHandler implements TemplateExceptionHandler {
        @Override
        public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
        }
    }

    public static class ValidationHashModel extends VariableTypeSupportHashModel {
        private final Map<String, FormVariableAccess> usedVariables = Maps.newHashMap();
        private final ProcessDefinition definition;

        public ValidationHashModel(ProcessDefinition definition) {
            Mode.setDesignerMode();
            this.definition = definition;
        }

        public Map<String, FormVariableAccess> getUsedVariables() {
            return usedVariables;
        }

        private TemplateModel wrapParameter(Variable variable) throws TemplateModelException {
            if (variable.getUserType() != null) {
                Map<String, TemplateModel> properties = new HashMap<String, TemplateModel>();
                for (Variable attribute : variable.getUserType().getAttributes()) {
                    properties.put(attribute.getName(), wrapParameter(attribute));
                }
                return new SimpleHash(properties);
            }
            return wrap(VAR_VALUE_PLC);
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            // add output variables / read access
            Variable variable = VariableUtils.getVariableByName(definition, key);
            if (variable != null) {
                if (!usedVariables.containsKey(key)) {
                    usedVariables.put(key, FormVariableAccess.READ);
                }
                if (stageRenderingParams) {
                    return wrapParameter(variable);
                }
                return getTemplateModel(variable);
            }
            if (MethodTag.hasTag(key)) {
                stageRenderingParams = true;
                return new ValidationMethodModel(key);
            }
            usedVariables.put(key, FormVariableAccess.DOUBTFUL);
            return wrap("noop");
        }

        private class ValidationMethodModel implements TemplateMethodModel {
            final String tagId;

            public ValidationMethodModel(String tagId) {
                this.tagId = tagId;
            }

            @Override
            public Object exec(List args) throws TemplateModelException {
                stageRenderingParams = false;
                MethodTag tag = MethodTag.getTagNotNull(tagId);
                int paramsSize = tag.params.size();
                if (paramsSize != args.size()) {
                    if (args.size() < paramsSize) {
                        paramsSize = args.size();
                    }
                    if (args.size() > paramsSize && tag.params.get(paramsSize - 1).multiple) {
                        paramsSize = args.size();
                    }
                }
                for (int i = 0; i < paramsSize; i++) {
                    String varName = (String) args.get(i);
                    MethodTag.Param param = tag.params.size() > i ? tag.params.get(i) : tag.params.get(tag.params.size() - 1);
                    if (param.variableAccess == VariableAccess.WRITE) {
                        usedVariables.put(varName, FormVariableAccess.WRITE);
                    } else if (param.variableAccess == VariableAccess.READ) {
                        if (!VAR_VALUE_PLC.equals(varName) && !usedVariables.containsKey(varName) && !param.isRichCombo()) {
                            usedVariables.put(varName, FormVariableAccess.READ);
                        }
                    }
                }
                return "noop";
            }
        }
    }
}
