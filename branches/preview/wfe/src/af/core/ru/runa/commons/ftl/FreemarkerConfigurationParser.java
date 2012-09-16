package ru.runa.commons.ftl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.runa.commons.xml.SimpleErrorHandler;

public class FreemarkerConfigurationParser {

    private static final Log log = LogFactory.getLog(FreemarkerConfigurationParser.class);
    private static final String CONFIG = "/freemarker-tags.xml";

    private static final String NAME_ATTR = "name";

    private static final String CLASS_ATTR = "class";

    private static final String VARTAG_ELEMENT = "configuration/ftltag";

    private FreemarkerConfigurationParser() {
    }

    public static FreemarkerConfiguration getConfiguration() throws IOException, SAXException {
        InputStream is = FreemarkerConfigurationParser.class.getResourceAsStream(CONFIG);
        if (is == null) {
            throw new IOException("No configuration found: " + CONFIG);
        }

        Digester digester = new Digester();
        digester.setErrorHandler(SimpleErrorHandler.getInstance());
        digester.push(new FreemarkerConfiguration());

        digester.addFactoryCreate(VARTAG_ELEMENT, new FtlTagCreationFactory());
        digester.addSetNestedProperties(VARTAG_ELEMENT);

        Rule r = new CallMethodRule(1, "registerVarTag", 2, new Class[] { String.class, FreemarkerTag.class });
        digester.addRule(VARTAG_ELEMENT, r);
        digester.addCallParam(VARTAG_ELEMENT, 0, NAME_ATTR);
        digester.addCallParam(VARTAG_ELEMENT, 1, true);

        return (FreemarkerConfiguration) digester.parse(is);
    }

    protected static class FtlTagCreationFactory extends AbstractObjectCreationFactory {

        @Override
        public Object createObject(Attributes attributes) {
            try {
                String className = attributes.getValue(CLASS_ATTR);
                return Class.forName(className).newInstance();
            } catch (Throwable e) {
                log.warn("Unable to load freemarker tag class", e);
                return null;
            }
        }
    }
}
