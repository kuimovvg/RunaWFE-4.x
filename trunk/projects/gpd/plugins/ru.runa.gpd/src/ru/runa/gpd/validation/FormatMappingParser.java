package ru.runa.gpd.validation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.util.XmlUtil;

public class FormatMappingParser {
    private static Map<String, FormatMapping> parseFormatMappings() {
        try {
            InputStream is = ValidatorParser.class.getResourceAsStream("formats_mapping.xml");
            Map<String, FormatMapping> formatMappings = new HashMap<String, FormatMapping>();
            Document doc = XmlUtil.parseWithoutValidation(is);
            List<Element> nodes = doc.getRootElement().elements("format");
            for (Element validatorElement : nodes) {
                String typeName = validatorElement.attributeValue("className");
                String name = validatorElement.attributeValue("name");
                String javaType = validatorElement.attributeValue("javaType");
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
