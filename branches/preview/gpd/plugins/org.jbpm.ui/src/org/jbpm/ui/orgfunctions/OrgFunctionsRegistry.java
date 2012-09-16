package ru.runa.bpm.ui.orgfunctions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.validation.FormatMapping;
import ru.runa.bpm.ui.validation.FormatMappingParser;

public class OrgFunctionsRegistry {

    private static final List<OrgFunctionDefinition> definitions = new ArrayList<OrgFunctionDefinition>();

    private static void init() {
        if (definitions.size() > 0) {
            return;
        }

        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.bpm.ui.orgfunctions").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    String nameKey = configElement.getAttribute("name");
                    OrgFunctionDefinition orgFunctionDefinition = new OrgFunctionDefinition(nameKey, configElement.getAttribute("className"));
                    IConfigurationElement[] parameterElements = configElement.getChildren();
                    for (IConfigurationElement paramElement : parameterElements) {
                        OrgFunctionParameter orgFunctionParameter = new OrgFunctionParameter(paramElement.getAttribute("name"), paramElement
                                .getAttribute("type"), Boolean.valueOf(paramElement.getAttribute("multiple")));
                        String initValue = paramElement.getAttribute("value");
                        if (initValue != null) {
                            orgFunctionParameter.setValue(initValue);
                        }
                        orgFunctionDefinition.addParameter(orgFunctionParameter);
                    }
                    orgFunctionDefinition.checkMultipleParameters();
                    definitions.add(orgFunctionDefinition);
                } catch (Exception e) {
                    DesignerLogger.logError("Error processing ru.runa.bpm.ui.orgfunctions element", e);
                }
            }
        }
    }

    public static List<OrgFunctionDefinition> getAllOrgFunctionDefinitions() {
        init();
        return definitions;
    }

    public static OrgFunctionDefinition getDefinitionByKey(String key) {
        for (OrgFunctionDefinition definition : getAllOrgFunctionDefinitions()) {
            if (key.equals(definition.getKey())) {
                return definition.getCopy();
            }
        }
        throw new RuntimeException("No OrgFunction found under key " + key);
    }

    public static OrgFunctionDefinition getDefinitionByName(String name) {
        if (name == null) {
            return OrgFunctionDefinition.DEFAULT.getCopy();
        }
        for (OrgFunctionDefinition definition : getAllOrgFunctionDefinitions()) {
            if (name.equals(definition.getName())) {
                return definition.getCopy();
            }
        }
        throw new RuntimeException("No OrgFunction found under name " + name);
    }

    public static OrgFunctionDefinition getDefinitionByClassName(String orgFunctionClassName) {
        for (OrgFunctionDefinition definition : getAllOrgFunctionDefinitions()) {
            if (orgFunctionClassName.equals(definition.getClassName())) {
                return definition.getCopy();
            }
        }
        throw new RuntimeException(OrgFunctionDefinition.MISSED_DEFINITION + " for: " + orgFunctionClassName);
    }

    public static OrgFunctionDefinition parseSwimlaneConfiguration(String swimlaneConfiguration) {
        if (swimlaneConfiguration.length() == 0) {
            return OrgFunctionDefinition.DEFAULT;
        }
        init();
        int startIndex = 0;
        String relationName = null;
        if (swimlaneConfiguration.startsWith("@")) {
            int leftBracketIndex = swimlaneConfiguration.indexOf("(");
            relationName = swimlaneConfiguration.substring(1, leftBracketIndex);
            startIndex = relationName.length() + 2;
        }
        int leftBracketIndex = swimlaneConfiguration.indexOf("(", startIndex);
        int rightBracketIndex = swimlaneConfiguration.indexOf(")");
        String orgFunctionName = swimlaneConfiguration.substring(startIndex, leftBracketIndex);
        OrgFunctionDefinition definition = getDefinitionByClassName(orgFunctionName);
        definition.setRelationName(relationName);
        String parametersString = swimlaneConfiguration.substring(leftBracketIndex + 1, rightBracketIndex);
        String[] parameters = parametersString.split(",", -1);
        int definitionParamsSize = definition.getParameters().size();
        if (parameters.length != definitionParamsSize) {
            OrgFunctionParameter lastParameter = definition.getParameters().get(definitionParamsSize - 1);
            if (definitionParamsSize < parameters.length && lastParameter.isMultiple()) {
                // last parameter is multiple
                definition.propagateParameter(lastParameter, parameters.length - definitionParamsSize);
            } else {
                throw new RuntimeException("Unapplicable parameters to org function: " + orgFunctionName);
            }
        }
        for (int i = 0; i < parameters.length; i++) {
            OrgFunctionParameter functionParameter = definition.getParameters().get(i);
            functionParameter.setValue(parameters[i]);
        }
        return definition;
    }

    public static Set<String> getVariableNames(ProcessDefinition processDefinition, String typeName) {
        Map<String, FormatMapping> mappings = FormatMappingParser.getFormatMappings();
        Set<String> formats = new HashSet<String>();
        for (FormatMapping mapping : mappings.values()) {
            if (typeName.equals(mapping.getName())) {
                formats.add(mapping.getTypeName());
            }
        }
        Set<String> variableNames = new TreeSet<String>();
        for (Variable variable : processDefinition.getVariablesList()) {
            if (formats.contains(variable.getFormat())) {
                variableNames.add(variable.getName());
            }
        }
        if (OrgFunctionParameter.TEXT_INPUT.equals(typeName)) {
            variableNames.addAll(processDefinition.getSwimlaneNames());
        }
        return variableNames;
    }

}
