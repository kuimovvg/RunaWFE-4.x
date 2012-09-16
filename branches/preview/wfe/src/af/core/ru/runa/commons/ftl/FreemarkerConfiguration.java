package ru.runa.commons.ftl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.commons.ApplicationContextFactory;

public class FreemarkerConfiguration {

    protected Log log = LogFactory.getLog(FreemarkerConfiguration.class);

    private final Map<String, FreemarkerTag> tags = new HashMap<String, FreemarkerTag>();

    public void registerVarTag(String name, FreemarkerTag freemarkerTag) {
        tags.put(name, freemarkerTag);
    }

    public FreemarkerTag getFreemarkerTag(String name) {
        if (!tags.containsKey(name)) {
            String possibleTagClassName = "ru.runa.wf.web.ftl.tags." + name + "Tag";
            try {
                FreemarkerTag tag = (FreemarkerTag) Class.forName(possibleTagClassName).newInstance();
                ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(tag);
                registerVarTag(name, tag);
            } catch (Exception e) {
                log.warn("Unable to load tag " + name + " as " + possibleTagClassName);
            }
        }
        return tags.get(name);
    }
}
