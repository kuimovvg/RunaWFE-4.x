package ru.runa.gpd.quick.formeditor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.quick.formeditor.util.QuickFormXMLUtil;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.collect.Maps;

public class QuickFormType extends FormType {
    public static final String READ_TAG = "DisplayVariable";
    public static final String WRITE_TAG = "InputVariable";

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, QuickFormEditor.ID, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, FormVariableAccess> getFormVariableNames(IFile formFile, FormNode formNode) throws Exception {
        Map<String, FormVariableAccess> variableNames = Maps.newHashMap();
        InputStream contentStream = formFile.getContents(true);
        if(contentStream != null && contentStream.available() != 0) {
        	Document document = XmlUtil.parseWithoutValidation(contentStream);
        	Element tagsElement = document.getRootElement().element(QuickFormXMLUtil.ELEMENT_TAGS);
            List<Element> varElementsList = tagsElement.elements(QuickFormXMLUtil.ELEMENT_TAG);
            for (Element varElement : varElementsList) {
                String tag = varElement.elementText(QuickFormXMLUtil.ATTRIBUTE_NAME);
                List<Element> paramElements = varElement.elements(QuickFormXMLUtil.ELEMENT_PARAM);
                if (paramElements != null && paramElements.size() > 0) {
                    for (Element paramElement : paramElements) {
                    	variableNames.put(paramElement.getText(), READ_TAG.equals(tag) ? FormVariableAccess.READ : FormVariableAccess.WRITE);
                    	break;
                    }
                }
            }
        }
        
        return variableNames;
    }

}
