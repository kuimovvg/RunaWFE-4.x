package tk.eclipse.plugin.forms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cyberneko.html.parsers.DOMParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.forms.FormType;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.util.ValidationUtil;
import ru.runa.bpm.ui.validation.ValidatorConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import tk.eclipse.plugin.wysiwyg.WYSIWYGHTMLEditor;

public abstract class BaseHtmlFormType extends FormType {
    private static final String EDITOR_ID = "tk.eclipse.plugin.wysiwyg.WYSIWYGHTMLEditor";

    private static final String READONLY_ATTR = "readonly";

    private static final String DISABLED_ATTR = "disabled";

    private static final String NAME_ATTR = "name";
    
    protected WYSIWYGHTMLEditor editor;

    @Override
    public IEditorPart openForm(final IFile formFile, final FormNode formNode) throws CoreException {
        editor = (WYSIWYGHTMLEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, EDITOR_ID, true);
        editor.setFormNode(formNode);
        editor.addPropertyListener(new IPropertyListener(){

            public void propertyChanged(Object source, int propId) {
                if (propId == WYSIWYGHTMLEditor.CLOSED && formFile.exists()) {
                    String op = "create";
                    try {
                        if (!formNode.hasFormValidation()) {
                        	String validationFileName = ValidationUtil.getFormValidationFileName(formNode.getName());
                            IFile validationFile = ValidationUtil.createNewValidationUsingForm(formFile, validationFileName, formNode);
                            formNode.setValidationFileName(validationFile.getName());
                        } else {
                            op = "update";
                            ValidationUtil.updateValidation(formFile, formNode);
                        }
                    } catch (Exception e) {
                        DesignerLogger.logError("Failed to " + op + " form validation", e);
                    }
                }
            }
        });
        return editor;
    }
    
    @Override
    public void validate(IFile formFile, FormNode formNode) {
        try {
            Map<String, Integer> formVars = getFormVariableNames(formFile, formNode);
            List<String> allVariableNames = formNode.getProcessDefinition().getVariableNames(true);
            Set<String> validationVariables = formNode.getValidationVariables((IFolder) formFile.getParent());
            for (String formVarName : formVars.keySet()) {
                if (formVars.get(formVarName) == FormType.DOUBTFUL) {
                    formNode.addWarning("formNode.formVariableTagUnknown", formVarName);
                } else if (!validationVariables.contains(formVarName) && formVars.get(formVarName) == FormType.WRITE_ACCESS) {
                    formNode.addWarning("formNode.formVariableOutOfValidation", formVarName);
                } else if (!allVariableNames.contains(formVarName)) {
                    // TODO addError("formNode.formVariableDoesNotExist", formVarName);
                    formNode.addWarning("formNode.formVariableDoesNotExist", formVarName);
                }
            }
            for (String validationVarName : validationVariables) {
                if (ValidatorConfig.GLOBAL_FIELD_ID.equals(validationVarName)) {
                    continue;
                }
                if (!formVars.keySet().contains(validationVarName)) {
                    formNode.addWarning("formNode.validationVariableOutOfForm", validationVarName);
                }
                if (!allVariableNames.contains(validationVarName)) {
                    formNode.addError("formNode.validationVariableDoesNotExist", validationVarName);
                } 
            }
        } catch (Exception e) {
            DesignerLogger.logErrorWithoutDialog("Error validating form: '" + getName() + "'", e);
            formNode.addWarning("formNode.validationUnknownError", e.getMessage());
        }
    }
    
    protected abstract Map<String, Integer> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception;

    @Override
    public String getFormFileName(IFile definitionFile, FormNode formNode) {
        return formNode.getName().concat(".").concat(formNode.getFormType());
    }

    @Override
    public Map<String, Integer> getFormVariableNames(IFile formFile, FormNode formNode) throws Exception {
        Map<String, Integer> variableNames = new HashMap<String, Integer>();

        byte[] formBytes = IOUtils.readStreamAsBytes(formFile.getContents(true));
        Document document = getDocument(new ByteArrayInputStream(formBytes));

        NodeList inputElements = document.getElementsByTagName("input");
        addHtmlFields(inputElements, variableNames);

        NodeList textareaElements = document.getElementsByTagName("textarea");
        addHtmlFields(textareaElements, variableNames);

        NodeList selectElements = document.getElementsByTagName("select");
        addHtmlFields(selectElements, variableNames);

        Map<String, Integer> typeSpecificVariableNames = getTypeSpecificVariableNames(formNode, formBytes);
        if (typeSpecificVariableNames != null) {
            variableNames.putAll(typeSpecificVariableNames);
        }
        
        return variableNames;
    }

    private static void addHtmlFields(NodeList inputElements, Map<String, Integer> variableNames) {
        for (int i = 0; i < inputElements.getLength(); i++) {
            Node nameNode = inputElements.item(i).getAttributes().getNamedItem(NAME_ATTR);
            Node disabledNode = inputElements.item(i).getAttributes().getNamedItem(DISABLED_ATTR);
            Node readonlyNode = inputElements.item(i).getAttributes().getNamedItem(READONLY_ATTR);
            if (nameNode != null) {
                boolean required = (disabledNode == null) && (readonlyNode == null);
                variableNames.put(nameNode.getNodeValue(), required ? WRITE_ACCESS : READ_ACCESS);
            }
        }
    }

    public static Document getDocument(InputStream is) throws IOException, SAXException {
        DOMParser parser = new DOMParser();
        InputSource inputSource = new InputSource(is);
        inputSource.setEncoding(PluginConstants.UTF_ENCODING);
        parser.parse(inputSource);
        return parser.getDocument();
    }
}
