package ru.runa.gpd.quick.formeditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.quick.formeditor.util.QuickFormXMLUtil;
import ru.runa.gpd.util.XmlUtil;

public class QuickFormType extends FormType {
	public static final String READ_TAG = "DisplayVariable";
	public static final String WRITE_TAG = "InputVariable";
	
	@Override
	public IEditorPart openForm(IFile formFile, FormNode formNode)
			throws CoreException {
		return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, QuickFormEditor.ID, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Integer> getFormVariableNames(IFile formFile,
			FormNode formNode) throws Exception {
		Map<String, Integer> variableNames = new HashMap<String, Integer>();		
		Document document = XmlUtil.parseWithoutValidation(formFile.getContents(true));
		
		List<Element> varElementsList = document.getRootElement().elements(QuickFormXMLUtil.TEMPLATE_VARIABLE);
		for (Element varElement : varElementsList) {
			String name = varElement.elementText(QuickFormXMLUtil.ATTRIBUTE_NAME);
			String tag = varElement.elementText(QuickFormXMLUtil.ATTRIBUTE_TAG);
			variableNames.put(name, READ_TAG.equals(tag) ? READ_ACCESS : WRITE_ACCESS);			
		}
        
        return variableNames;
	}

	@Override
	public void validate(IFile formFile, FormNode formNode, List<ValidationError> errors) {
		try {
			Map<String, Integer> formVars = getFormVariableNames(formFile, formNode);
	        List<String> allVariableNames = formNode.getProcessDefinition().getVariableNames(true);
	        for (String formVarName : formVars.keySet()) {
	            if (!allVariableNames.contains(formVarName)) {
	                String message = WYSIWYGPlugin.getResourceString("validation.formVariableDoesNotExist", formVarName);
	                errors.add(ValidationError.createWarning(formNode, message));
	            }
	        }
		} catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Error validating form: '" + getName() + "'", e);
            String message = WYSIWYGPlugin.getResourceString("validation.validationUnknownError", e);
            errors.add(ValidationError.createWarning(formNode, message));
        }		
	}

}
