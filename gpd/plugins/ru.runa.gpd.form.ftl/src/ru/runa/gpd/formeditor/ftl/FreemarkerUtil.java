package ru.runa.gpd.formeditor.ftl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.formeditor.BaseHtmlFormType;
import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import freemarker.Mode;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

public class FreemarkerUtil {
    public static final String IMAGE_DIR = "/editor/plugins/FreemarkerTags/im/";

    private static String METHOD_ELEMENT_NAME() {
        return WYSIWYGPlugin.useCKEditor3() ? "ftl_element" : "img";
    }

    private static String OUTPUT_ELEMENT_NAME() {
        return WYSIWYGPlugin.useCKEditor3() ? "ftl_element_output" : "img";
    }

    private static final String ATTR_FTL_TAG_FORMAT = "ftltagformat";
    private static final String ATTR_FTL_TAG_PARAMS = "ftltagparams";
    private static final String ATTR_FTL_TAG_NAME = "ftltagname";
    private static final String ATTR_STYLE = "style";

    public static String transformFromHtml(String html, List<String> variableNames) throws SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
        Document document = BaseHtmlFormType.getDocument(new ByteArrayInputStream(html.getBytes(PluginConstants.UTF_ENCODING)));
        NodeList spanElements = document.getElementsByTagName(METHOD_ELEMENT_NAME());
        int len = spanElements.getLength();
        int idx = 0;
        for (int i = 0; i < len; i++) {
            Node domNode = spanElements.item(idx);
            String tagName = getAttrValue(domNode, ATTR_FTL_TAG_NAME);
            if (tagName == null) {
                continue;
            }
            String tagParams = getAttrValue(domNode, ATTR_FTL_TAG_PARAMS);
            if (tagParams != null) {
                // Method
                StringBuffer ftlTag = new StringBuffer();
                ftlTag.append("${").append(tagName);
                ftlTag.append("(");
                if (tagParams.length() > 0) {
                    String[] params = tagParams.split("\\|");
                    for (int j = 0; j < params.length; j++) {
                        if (j != 0) {
                            ftlTag.append(", ");
                        }
                        boolean surroundWithBrackets = true;
                        try {
                            Param param = MethodTag.getTag(tagName).params.get(j);
                            if (param.isVarCombo() || (param.isRichCombo() && variableNames.contains(params[j]))) {
                                surroundWithBrackets = false;
                            }
                        } catch (Exception e) {
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
            } else {
                ++idx;
            }
        }
        spanElements = document.getElementsByTagName(OUTPUT_ELEMENT_NAME());
        len = spanElements.getLength();
        idx = 0;
        for (int i = 0; i < len; i++) {
            Node domNode = spanElements.item(idx);
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
                ++idx;
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeHtml(document, os);
        return new String(os.toByteArray(), PluginConstants.UTF_ENCODING);
    }

    private static void writeHtml(Document document, OutputStream os) throws TransformerFactoryConfigurationError, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "true");
        transformer.setOutputProperty(OutputKeys.ENCODING, PluginConstants.UTF_ENCODING);
        transformer.transform(new DOMSource(document), new StreamResult(os));
    }

    private static String getAttrValue(Node domNode, String attr) {
        Node attrNode = domNode.getAttributes().getNamedItem(attr);
        if (attrNode == null) {
            return null;
        }
        return attrNode.getNodeValue();
    }

    public static String transformToHtml(Set<String> variableNames, String ftlText) throws IOException, TemplateException {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setLocalizedLookup(false);
        Template template = new Template("test", new StringReader(ftlText), cfg, PluginConstants.UTF_ENCODING);
        StringWriter out = new StringWriter();
        template.process(new EditorHashModel(variableNames), out);
        out.flush();
        return out.toString();
    }

    @SuppressWarnings("serial")
    public static class EditorHashModel extends SimpleHash {
        private final Set<String> variableNames;
        private boolean stageRenderingParams = false;

        public EditorHashModel(Set<String> variableNames) {
            Mode.setDesignerMode();
            this.variableNames = variableNames;
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            // output variables
            if (this.variableNames.contains(key)) {
                if (stageRenderingParams) {
                    return new SimpleScalar(key);
                } else {
                    return new SpanScalarModel(key);
                }
            }
            stageRenderingParams = true;
            return new SpanMethodModel(key);
        }

        public class SpanScalarModel implements TemplateScalarModel {
            private final String name;

            public SpanScalarModel(String name) {
                this.name = name;
            }

            @Override
            public String getAsString() throws TemplateModelException {
                StringBuffer buffer = new StringBuffer("<").append(OUTPUT_ELEMENT_NAME()).append(" ");
                buffer.append(ATTR_FTL_TAG_NAME).append("=\"").append(name).append("\" ");
                buffer.append("ftlTagFormat='-' ");
                buffer.append(ATTR_STYLE).append("=\"").append(getStyle(null)).append("\" ");
                buffer.append("src=\"http://localhost:48780/editor/FreemarkerTags.java?method=GetTagImage&tagName=").append(name).append("\" ");
                buffer.append("/>");
                return buffer.toString();
            }
        }

        public class SpanMethodModel implements TemplateMethodModel {
            private String name;

            public SpanMethodModel(String name) {
                this.name = name;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Object exec(List args) throws TemplateModelException {
                stageRenderingParams = false;
                StringBuffer buffer = new StringBuffer("<").append(METHOD_ELEMENT_NAME()).append(" ");
                buffer.append("src=\"http://localhost:48780/editor/FreemarkerTags.java?method=GetTagImage&tagName=").append(name).append("\" ");
                buffer.append(ATTR_FTL_TAG_NAME).append("=\"").append(name).append("\" ");
                buffer.append(ATTR_FTL_TAG_PARAMS).append("=\"");
                for (int i = 0; i < args.size(); i++) {
                    if (i != 0) {
                        buffer.append("|");
                    }
                    buffer.append(args.get(i).toString());
                }
                buffer.append("\" ");
                buffer.append(ATTR_STYLE).append("=\"").append(getStyle(name)).append("\" ");
                buffer.append("/>");
                return buffer.toString();
            }
        }
    }

    protected static String getStyle(String tagName) {
        int w = 250;
        int h = 40;
        if (tagName != null && MethodTag.hasTag(tagName)) {
            MethodTag tag = MethodTag.getTag(tagName);
            w = tag.width;
            h = tag.height;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("width: ").append(w).append("px; ");
        buffer.append("height: ").append(h).append("px; ");
        return buffer.toString();
    }

    public static class TagParser {
        private static Map<String, FormatMapping> formatMappings;

        public static FormatMapping getFormatMapping(String key) {
            if (key == null || key.length() == 0) {
                key = "ru.runa.wfe.var.format.StringFormat";
            }
            if (!getFormatMappings().containsKey(key)) {
                return new FormatMapping(key, key);
            }
            return getFormatMappings().get(key);
        }

        private static Map<String, FormatMapping> getFormatMappings() {
            if (formatMappings == null) {
                formatMappings = parseFormatMappingsInternal();
            }
            return formatMappings;
        }

        private static Document getDocument(InputStream is) throws SAXException, IOException, ParserConfigurationException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            return documentBuilder.parse(is);
        }

        private static Map<String, FormatMapping> parseFormatMappingsInternal() {
            try {
                Map<String, FormatMapping> formatMappings = new HashMap<String, FormatMapping>();
                Document doc = getDocument(FreemarkerUtil.class.getResourceAsStream("formats_mapping.xml"));
                NodeList nodes = doc.getElementsByTagName("format");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element validatorElement = (Element) nodes.item(i);
                    String typeName = validatorElement.getAttribute("className");
                    String name = validatorElement.getAttribute("name");
                    FormatMapping formatMapping = new FormatMapping(typeName, name);
                    formatMappings.put(typeName, formatMapping);
                }
                return formatMappings;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
