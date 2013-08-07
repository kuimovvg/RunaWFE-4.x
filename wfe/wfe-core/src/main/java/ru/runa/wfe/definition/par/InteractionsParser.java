/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.definition.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * Created on 17.11.2004
 * 
 */
public class InteractionsParser implements ProcessArchiveParser {
    private final static String FORM_ELEMENT_NAME = "form";
    private final static String STATE_ATTRIBUTE_NAME = "state";
    private final static String FILE_ATTRIBUTE_NAME = "file";
    private static final String VALIDATION_FILE_ATTRIBUTE_NAME = "validationFile";
    private static final String SCRIPT_FILE_ATTRIBUTE_NAME = "scriptFile";
    private static final String JS_VALIDATION_ATTRIBUTE_NAME = "jsValidation";
    private final static String TYPE_ATTRIBUTE_NAME = "type";

    @Override
    public void readFromArchive(ProcessArchive archive, ProcessDefinition processDefinition) {
        try {
            byte[] formsXml = archive.getFileData(IFileDataProvider.FORMS_XML_FILE_NAME);
            if (formsXml == null) {
                return;
            }
            Document document = XmlUtils.parseWithoutValidation(formsXml);
            List<Element> formElements = document.getRootElement().elements(FORM_ELEMENT_NAME);
            for (Element formElement : formElements) {
                String stateId = formElement.attributeValue(STATE_ATTRIBUTE_NAME);
                Node node = processDefinition.getNodeNotNull(stateId);
                String fileName = formElement.attributeValue(FILE_ATTRIBUTE_NAME);
                String typeName = formElement.attributeValue(TYPE_ATTRIBUTE_NAME);
                String validationFileName = formElement.attributeValue(VALIDATION_FILE_ATTRIBUTE_NAME);
                boolean jsValidationEnabled = Boolean.parseBoolean(formElement.attributeValue(JS_VALIDATION_ATTRIBUTE_NAME));
                String scriptFileName = formElement.attributeValue(SCRIPT_FILE_ATTRIBUTE_NAME);

                byte[] formCode = null;
                if (!Strings.isNullOrEmpty(fileName)) {
                    formCode = archive.getFileDataNotNull(fileName);
                }
                byte[] validationXml = null;
                if (!Strings.isNullOrEmpty(validationFileName)) {
                    validationXml = archive.getFileDataNotNull(validationFileName);
                }
                byte[] scriptJs = null;
                if (!Strings.isNullOrEmpty(scriptFileName)) {
                    scriptJs = archive.getFileDataNotNull(scriptFileName);
                }
                byte[] css = archive.getFileData(IFileDataProvider.FORM_CSS_FILE_NAME);
                Interaction interaction = new Interaction(node.getName(), node.getDescription(), typeName, formCode, validationXml,
                        jsValidationEnabled, scriptJs, css);
                if (validationXml != null) {
                    List<String> variableNames = ValidationXmlParser.readVariableNames(processDefinition, validationFileName, validationXml);
                    List<String> requiredVarNames = ValidationXmlParser.readRequiredVariableNames(processDefinition, validationXml);
                    for (String varName : requiredVarNames) {
                        interaction.getRequiredVariableNames().add(varName);
                    }
                    for (String name : variableNames) {
                        VariableDefinition variableDefinition = processDefinition.getVariable(name, true);
                        if (variableDefinition == null) {
                            throw new InvalidDefinitionException(processDefinition.getName(), "Variable '" + name + "' is defined in '"
                                    + validationFileName + "' but not defined in " + processDefinition);
                        }
                        interaction.getVariables().put(name, variableDefinition);
                    }
                }
                processDefinition.addInteraction(stateId, interaction);
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(processDefinition.getName(), e);
        }
    }

}
