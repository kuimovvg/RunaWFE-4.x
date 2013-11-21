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
            List<Element> varElementsList = document.getRootElement().elements(QuickFormXMLUtil.TEMPLATE_VARIABLE);
            for (Element varElement : varElementsList) {
                String name = varElement.elementText(QuickFormXMLUtil.ATTRIBUTE_NAME);
                String tag = varElement.elementText(QuickFormXMLUtil.ATTRIBUTE_TAG);
                variableNames.put(name, READ_TAG.equals(tag) ? FormVariableAccess.READ : FormVariableAccess.WRITE);
            }
        }
        
        return variableNames;
    }

}
