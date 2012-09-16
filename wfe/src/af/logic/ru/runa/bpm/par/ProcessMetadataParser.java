package ru.runa.bpm.par;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class ProcessMetadataParser {

    public static Map<String, String> parseOrgFunctionMappings(byte[] bytes) throws IOException, SAXException {
        Preconditions.checkNotNull(bytes);
        Digester digester = new Digester();
        digester.push(new HashMap<String, String>());
        digester.addRule("functions/function", new Rule() {
            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {
                ((Map<String, String>) getDigester().peek()).put(attributes.getValue("function"), attributes.getValue("name"));
                super.begin(namespace, name, attributes);
            }
        });
        return (Map<String, String>) digester.parse(new ByteArrayInputStream(bytes));
    }

}
