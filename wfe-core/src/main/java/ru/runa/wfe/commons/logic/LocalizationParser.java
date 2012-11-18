package ru.runa.wfe.commons.logic;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class LocalizationParser {
    private static final Log log = LogFactory.getLog(LocalizationParser.class);

    @SuppressWarnings("unchecked")
    public static Map<String, String> parseLocalizations(InputStream stream) {
        Preconditions.checkNotNull(stream, "No localization data to parse.");
        Map<String, String> localizations = Maps.newHashMap();
        try {
            Document document = XmlUtils.parseWithoutValidation(stream);
            Element root = document.getRootElement();
            List<Element> elements = root.elements("message");
            for (Element element : elements) {
                localizations.put(element.attributeValue("name"), element.attributeValue("value"));
            }
        } catch (Exception e) {
            log.error("Unable parse localizations", e);
        }
        return localizations;
    }

}
