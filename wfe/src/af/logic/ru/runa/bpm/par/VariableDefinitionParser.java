package ru.runa.bpm.par;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.wf.ProcessDefinitionXMLFormatException;
import ru.runa.wf.form.VariableDefinition;

public class VariableDefinitionParser implements ProcessArchiveParser {

    @Override
    public void readFromArchive(ProcessArchive archive, ExecutableProcessDefinition processDefinition) {
        try {
            byte[] bytes = archive.getFileDataNotNull(FileDataProvider.VARIABLES_XML_FILE_NAME);
            Digester digester = new Digester();
            digester.push(new ListOrderedMap());
            digester.addRule("variables/variable", new Rule() {

                @Override
                public void begin(String namespace, String elementName, Attributes attributes) throws Exception {
                    String name = attributes.getValue("name");
                    String format = attributes.getValue("format");
                    String isPublicStr = attributes.getValue("public");
                    boolean isPublic = isPublicStr != null ? Boolean.parseBoolean(isPublicStr) : false;
                    String defaultValue = attributes.getValue("defaultValue");
                    ((Map<String, VariableDefinition>) getDigester().peek()).put(name, new VariableDefinition(name, format, isPublic, defaultValue));
                    super.begin(namespace, elementName, attributes);
                }
            });
            Map<String, VariableDefinition> map = (Map<String, VariableDefinition>) digester.parse(new ByteArrayInputStream(bytes));
            processDefinition.getVariables().putAll(map);
        } catch (IOException e) {
            throw new InternalApplicationException(e);
        } catch (SAXException e) {
            throw new ProcessDefinitionXMLFormatException(FileDataProvider.VARIABLES_XML_FILE_NAME, e);
        }
    }

}
