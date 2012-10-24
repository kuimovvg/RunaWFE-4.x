package org.jbpm.ui.par;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FormsXmlContentProvider extends AuxContentProvider {
    public static final String FORMS_XSD_FILE_NAME = "forms.xsd";
    public static final String FORMS_XML_FILE_NAME = "forms.xml";

    private static final String TYPE_ATTRIBUTE_NAME = "type";

    private static final String FILE_ATTRIBUTE_NAME = "file";

    private static final String VALIDATION_FILE_ATTRIBUTE_NAME = "validationFile";

    private static final String JS_VALIDATION_ATTRIBUTE_NAME = "jsValidation";

    private static final String SCRIPT_FILE_ATTRIBUTE_NAME = "scriptFile";

    private static final String STATE_ATTRIBUTE_NAME = "state";

    private static final String FORM_ELEMENT_NAME = "form";

    private static final String FORMS_ELEMENT_NAME = "forms";

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception {
        IFile formsFile = folder.getFile(FORMS_XML_FILE_NAME);
        if (!formsFile.exists()) {
            return;
        }
        Document document = XmlUtil.parseDocument(formsFile.getContents());
        NodeList formElementsList = document.getDocumentElement().getElementsByTagName(FORM_ELEMENT_NAME);
        for (int i = 0; i < formElementsList.getLength(); i++) {
            Element formElement = (Element) formElementsList.item(i);
            String stateName = formElement.getAttribute(STATE_ATTRIBUTE_NAME);
            Node node = definition.getNodeByNameNotNull(stateName);
            if (node instanceof FormNode) {
                FormNode formNode = (FormNode) node;
                String typeName = formElement.getAttribute(TYPE_ATTRIBUTE_NAME);
                if (!isEmptyOrNull(typeName)) {
                    formNode.setFormType(typeName);
                }
                String fileName = formElement.getAttribute(FILE_ATTRIBUTE_NAME);
                if (!isEmptyOrNull(fileName)) {
                    formNode.setFormFileName(fileName);
                }
                String validationFileName = formElement.getAttribute(VALIDATION_FILE_ATTRIBUTE_NAME);
                if (!isEmptyOrNull(validationFileName)) {
                    formNode.setValidationFileName(validationFileName);
                    boolean useJsValidation = false;
                    String useJsAttr = formElement.getAttribute(JS_VALIDATION_ATTRIBUTE_NAME);
                    if ((useJsAttr != null) && (useJsAttr.length() > 0)) {
                        useJsValidation = Boolean.parseBoolean(useJsAttr);
                    }
                    formNode.setUseJSValidation(useJsValidation);
                }
                String scriptFileName = formElement.getAttribute(SCRIPT_FILE_ATTRIBUTE_NAME);
                if (!isEmptyOrNull(scriptFileName)) {
                    formNode.setScriptFileName(scriptFileName);
                }
            }
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(FORMS_ELEMENT_NAME, FORMS_XSD_FILE_NAME);
        Element root = document.getDocumentElement();
        for (Node node : definition.getNodes()) {
            if (node instanceof FormNode) {
                FormNode formNode = (FormNode) node;
                if (formNode.hasForm() || formNode.hasFormValidation()) {
                    Element formElement = document.createElement(FORM_ELEMENT_NAME);
                    formElement.setAttribute(STATE_ATTRIBUTE_NAME, formNode.getName());
                    root.appendChild(formElement);
                    if (formNode.hasForm()) {
                        formElement.setAttribute(FILE_ATTRIBUTE_NAME, formNode.getFormFileName());
                        formElement.setAttribute(TYPE_ATTRIBUTE_NAME, formNode.getFormType());
                    }
                    if (formNode.hasFormValidation()) {
                        formElement.setAttribute(VALIDATION_FILE_ATTRIBUTE_NAME, formNode.getValidationFileName());
                        formElement.setAttribute(JS_VALIDATION_ATTRIBUTE_NAME, String.valueOf(formNode.isUseJSValidation()));
                    }
                    if (formNode.hasFormScript()) {
                        formElement.setAttribute(SCRIPT_FILE_ATTRIBUTE_NAME, formNode.getScriptFileName());
                    }
                }
            }
        }
        byte[] bytes = XmlUtil.writeXml(document);
        updateFile(folder.getFile(FORMS_XML_FILE_NAME), bytes);
    }

}
