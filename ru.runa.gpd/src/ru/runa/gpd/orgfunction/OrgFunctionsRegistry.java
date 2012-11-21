package ru.runa.gpd.orgfunction;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.handler.ArtifactContentProvider;
import ru.runa.gpd.handler.ArtifactRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.validation.FormatMapping;
import ru.runa.gpd.validation.FormatMappingParser;

public class OrgFunctionsRegistry extends ArtifactRegistry<OrgFunctionDefinition> {
    private static final OrgFunctionsRegistry instance = new OrgFunctionsRegistry();

    public static OrgFunctionsRegistry getInstance() {
        return instance;
    }

    @Override
    protected File getContentFile() {
        return null;
    }

    public OrgFunctionsRegistry() {
        super(new ArtifactContentProvider<OrgFunctionDefinition>());
    }

    @Override
    protected void loadDefaults(List<OrgFunctionDefinition> list) {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.orgFunctions").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    String className = configElement.getAttribute("className");
                    String label = configElement.getAttribute("label");
                    OrgFunctionDefinition orgFunctionDefinition = new OrgFunctionDefinition(className, label);
                    IConfigurationElement[] parameterElements = configElement.getChildren();
                    for (IConfigurationElement paramElement : parameterElements) {
                        OrgFunctionParameter orgFunctionParameter = new OrgFunctionParameter(paramElement.getAttribute("name"), paramElement.getAttribute("type"),
                                Boolean.valueOf(paramElement.getAttribute("multiple")));
                        String initValue = paramElement.getAttribute("value");
                        if (initValue != null) {
                            orgFunctionParameter.setValue(initValue);
                        }
                        orgFunctionDefinition.addParameter(orgFunctionParameter);
                    }
                    orgFunctionDefinition.checkMultipleParameters();
                    list.add(orgFunctionDefinition);
                } catch (Exception e) {
                    PluginLogger.logError("Error processing 'orgFunctions' element", e);
                }
            }
        }
    }

    public static OrgFunctionDefinition parseSwimlaneConfiguration(String swimlaneConfiguration) {
        if (swimlaneConfiguration.length() == 0) {
            return OrgFunctionDefinition.DEFAULT;
        }
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
        OrgFunctionDefinition definition = getInstance().getArtifactNotNull(orgFunctionName);
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

    // TODO move to VariableFormatRegistry
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
