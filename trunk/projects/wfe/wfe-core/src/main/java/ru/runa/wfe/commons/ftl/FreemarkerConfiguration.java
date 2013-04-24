package ru.runa.wfe.commons.ftl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class FreemarkerConfiguration {
    private Log log = LogFactory.getLog(FreemarkerConfiguration.class);
    private static final String CONFIG = "freemarker-tags.xml";
    private static final String TAG_ELEMENT = "ftltag";
    private static final String NAME_ATTR = "name";
    private static final String CLASS_ATTR = "class";
    private final Map<String, Class<? extends FreemarkerTag>> tags = Maps.newHashMap();

    private static FreemarkerConfiguration instance;

    public static FreemarkerConfiguration getInstance(Class<?> clazz) {
        if (instance == null) {
            instance = new FreemarkerConfiguration(clazz);
        }
        return instance;
    }

    public String getRegistrationInfo() {
        return Joiner.on(", ").join(tags.values());
    }

    private FreemarkerConfiguration(Class<?> clazz) {
        InputStream is = ClassLoaderUtil.getAsStreamNotNull(CONFIG, clazz);
        log.info("Using " + is);
        Document document = XmlUtils.parseWithoutValidation(is);
        Element root = document.getRootElement();
        List<Element> tagElements = root.elements(TAG_ELEMENT);
        for (Element tagElement : tagElements) {
            try {
                String name = tagElement.attributeValue(NAME_ATTR);
                String className = tagElement.attributeValue(CLASS_ATTR);
                Class<? extends FreemarkerTag> tagClass = (Class<? extends FreemarkerTag>) ClassLoaderUtil.loadClass(className);
                addTag(name, tagClass);
            } catch (Throwable e) {
                log.warn("Unable to create freemarker tag", e);
            }
        }
    }

    private void addTag(String name, Class<? extends FreemarkerTag> tagClass) throws Exception {
        // test creation
        tagClass.newInstance();
        tags.put(name, tagClass);
        log.debug("Registered tag " + name + " as " + tagClass);
    }

    public FreemarkerTag getTag(String name) {
        if (!tags.containsKey(name)) {
            String possibleTagClassName = "ru.runa.wf.web.ftl.method." + name + "Tag";
            try {
                Class<? extends FreemarkerTag> tagClass = (Class<? extends FreemarkerTag>) ClassLoaderUtil.loadClass(possibleTagClassName);
                addTag(name, tagClass);
            } catch (Exception e) {
                log.warn("Unable to load tag " + name + " as " + possibleTagClassName + ". Check your " + CONFIG);
                return null;
            }
        }
        Class<? extends FreemarkerTag> tagClass = tags.get(name);
        return ApplicationContextFactory.createAutowiredBean(tagClass);
    }
}
