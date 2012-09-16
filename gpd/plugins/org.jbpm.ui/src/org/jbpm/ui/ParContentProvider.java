package ru.runa.bpm.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.par.ActionDescriptionContentProvider;
import ru.runa.bpm.ui.par.AuxContentProvider;
import ru.runa.bpm.ui.par.FormsXmlContentProvider;
import ru.runa.bpm.ui.par.GpdXmlContentProvider;
import ru.runa.bpm.ui.par.SubstitutionExceptionsXmlContentProvider;
import ru.runa.bpm.ui.par.SwimlaneGUIContentProvider;
import ru.runa.bpm.ui.par.VariablesXmlContentProvider;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.validation.ValidatorConfig;
import ru.runa.bpm.ui.validation.ValidatorParser;

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
    }

    // Writing the auxiliary info.
    public static void saveAuxInfo(IFile definitionFile, ProcessDefinition definition) {
        try {
            IFolder folder = (IFolder) definitionFile.getParent();
            for (AuxContentProvider contentProvider : contentProviders) {
                contentProvider.saveToFile(folder, definition);
            }
        } catch (Exception e) {
            DesignerLogger.logError(e);
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
