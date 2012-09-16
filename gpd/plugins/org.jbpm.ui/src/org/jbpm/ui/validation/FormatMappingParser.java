package ru.runa.bpm.ui.validation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FormatMappingParser {

    private static Map<String, FormatMapping> parseFormatMappings() {
        try {
            InputStream is = ValidatorParser.class.getResourceAsStream("formats_mapping.xml");
            Map<String, FormatMapping> formatMappings = new HashMap<String, FormatMapping>();
            Document doc = XmlUtil.parseDocument(is);
            NodeList nodes = doc.getElementsByTagName("format");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element validatorElement = (Element) nodes.item(i);
                String typeName = validatorElement.getAttribute("className");
                String name = validatorElement.getAttribute("name");
                String javaType = validatorElement.getAttribute("javaType");
                FormatMapping formatMapping = new FormatMapping(typeName, name, javaType);
                formatMappings.put(typeName, formatMapping);
            }
            return formatMappings;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, FormatMapping> formatMappings;
    public static Map<String, FormatMapping> getFormatMappings() {
        if (formatMappings == null) {
            formatMappings = FormatMappingParser.parseFormatMappings();
        }
        return formatMappings;
    }

}
