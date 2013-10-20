package ru.runa.gpd.formeditor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil.MyTemplateExceptionHandler;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil.ValidationHashModel;
import ru.runa.gpd.formeditor.ftl.MethodTag;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.ftl.validation.FreemarkerTag;
import ru.runa.gpd.formeditor.ftl.validation.FreemarkerTagValidator;
import ru.runa.gpd.formeditor.ftl.validation.FtlFormParser;
import ru.runa.gpd.formeditor.ftl.validation.ValidationMessage;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.view.ValidationErrorsView;
import ru.runa.gpd.util.IOUtils;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FtlFormType extends BaseHtmlFormType {
    private static final String FREEMARKER_TAG_VALIDATOR_ID = "ru.runa.gpd.form.ftl.freemarker_tag_validator";
    private static List<FreemarkerTagValidator> freemarkerTagValidators;

    @Override
    protected Map<String, Integer> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setLocalizedLookup(false);
        cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
        String string = new String(formBytes, "UTF-8");
        Template template = new Template("test", new StringReader(string), cfg, "UTF-8");
        StringWriter out = new StringWriter();
        ValidationHashModel validationHashModel = new ValidationHashModel(formNode.getProcessDefinition());
        template.process(validationHashModel, out);
        return validationHashModel.getUsedVariables();
    }

    @Override
    public void validate(IFile formFile, FormNode formNode) {
        clearValidationMessages(formFile);

        super.validate(formFile, formNode);
        validateFreemarkerTags(formFile, formNode);
    }

    public void validateFormContent(IFile formFile, FormNode formNode) {
        clearValidationMessages(formFile);
        validateFreemarkerTags(formFile, formNode);
    }

    private void validateFreemarkerTags(IFile formFile, FormNode formNode) {
        if (freemarkerTagValidators == null)
            initFreemarkerTagValidators();

        try {
            String form = IOUtils.readStream(formFile.getContents());
            List<Variable> variableList = formNode.getProcessDefinition().getVariables(true);
            Map<String, Variable> variables = new HashMap<String, Variable>();
            for (Variable variable : variableList)
                variables.put(variable.getName(), variable);

            List<FreemarkerTag> freemarkerTags = FtlFormParser.getFreemarkerTags(form);
            for (FreemarkerTagValidator validator : freemarkerTagValidators) {
                for (FreemarkerTag freemarkerTag : freemarkerTags) {
                    List<ValidationMessage> validationMessages = validator.validateTag(freemarkerTag, variables);
                    for (ValidationMessage validationMessage : validationMessages) {
                        showValidationError(formFile, formNode, freemarkerTag, validationMessage);
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Error validating form: '" + getName() + "'", e);
        }
    }

    private void clearValidationMessages(IFile formFile) {
        try {
            formFile.deleteMarkers(ValidationErrorsView.ID, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    private synchronized void initFreemarkerTagValidators() {
        if (freemarkerTagValidators != null)
            return;

        freemarkerTagValidators = new LinkedList<FreemarkerTagValidator>();
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(FREEMARKER_TAG_VALIDATOR_ID);
        try {
            for (IConfigurationElement e : config) {
                System.out.println("Evaluating extension");
                final Object o = e.createExecutableExtension("class");
                if (o instanceof FreemarkerTagValidator) {
                    freemarkerTagValidators.add((FreemarkerTagValidator) o);
                }
            }
        } catch (CoreException ex) {
            WYSIWYGPlugin.logError("Unable to load freemarker tag validators", ex);
        }
    }

    private void showValidationError(IFile formFile, FormNode formNode, FreemarkerTag freemarkerTag, ValidationMessage validationMessage) {
        try {
            IMarker marker = formFile.createMarker(ValidationErrorsView.ID);
            if (marker.exists()) {
                marker.setAttribute(IMarker.MESSAGE, validationMessage.message);
                MethodTag methodTag = MethodTag.getTagNotNull(freemarkerTag.getName());
                String elementSource = methodTag.name;
                marker.setAttribute(IMarker.LOCATION, elementSource);

                int severity;
                switch (validationMessage.severity) {
                case ERROR:
                    severity = IMarker.SEVERITY_ERROR;
                    break;
                case WARNING:
                    severity = IMarker.SEVERITY_WARNING;
                    break;
                default:
                    severity = IMarker.SEVERITY_WARNING;
                    break;
                }
                String processName = formNode.getProcessDefinition().getName();
                marker.setAttribute(IMarker.SEVERITY, severity);
                marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, processName);
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }
}
