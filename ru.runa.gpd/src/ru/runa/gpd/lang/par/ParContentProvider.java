package ru.runa.gpd.lang.par;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorParser;

public class ParContentProvider {
    public static final String PROCESS_DEFINITION_FILE_NAME = "processdefinition.xml";
    public static final String PROCESS_DEFINITION_DESCRIPTION_FILE_NAME = "index.html";
    public static final String FORM_CSS_FILE_NAME = "form.css";
    public static final String PROCESS_IMAGE_FILE_NAME = "processimage.jpg";
    public static final String PROCESS_INSTANCE_START_IMAGE_FILE_NAME = "start.png";
    private static final List<AuxContentProvider> contentProviders = new ArrayList<AuxContentProvider>();
    static {
        contentProviders.add(new VariablesXmlContentProvider());
        contentProviders.add(new FormsXmlContentProvider());
        contentProviders.add(new GpdXmlContentProvider());
        contentProviders.add(new SwimlaneGUIContentProvider());
        contentProviders.add(new ActionDescriptionContentProvider());
        contentProviders.add(new SubstitutionExceptionsXmlContentProvider());
        contentProviders.add(new BotsXmlContentProvider());
    }

    // Writing the auxiliary info.
    public static void saveAuxInfo(IFile definitionFile, ProcessDefinition definition) {
        try {
            IFolder folder = (IFolder) definitionFile.getParent();
            for (AuxContentProvider contentProvider : contentProviders) {
                contentProvider.saveToFile(folder, definition);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    // Reading the auxiliary info.
    public static void readAuxInfo(IFile definitionFile, ProcessDefinition definition) throws Exception {
        IFolder folder = (IFolder) definitionFile.getParent();
        for (AuxContentProvider contentProvider : contentProviders) {
            contentProvider.readFromFile(folder, definition);
        }
    }

    public static List<FormNode> getFormsWhereVariableUsed(IFile definitionFile, ProcessDefinition definition, Variable variable) {
        List<FormNode> result = new ArrayList<FormNode>();
        List<FormNode> allNodes = definition.getChildren(FormNode.class);
        for (FormNode formNode : allNodes) {
            if (formNode.hasFormValidation()) {
                IFile validationFile = IOUtils.getAdjacentFile(definitionFile, formNode.getValidationFileName());
                Map<String, Map<String, ValidatorConfig>> config = ValidatorParser.parseValidatorConfigs(validationFile);
                if (config.containsKey(variable.getName())) {
                    result.add(formNode);
                }
            }
        }
        return result;
    }

    public static void rewriteFormValidationsRemoveVariable(IFile definitionFile, List<FormNode> formNodes, Variable variable) {
        for (FormNode formNode : formNodes) {
            IFile validationFile = IOUtils.getAdjacentFile(definitionFile, formNode.getValidationFileName());
            Map<String, Map<String, ValidatorConfig>> fieldConfigs = ValidatorParser.parseValidatorConfigs(validationFile);
            fieldConfigs.remove(variable.getName());
            ValidatorParser.writeValidatorXml(validationFile, fieldConfigs);
        }
    }
}
