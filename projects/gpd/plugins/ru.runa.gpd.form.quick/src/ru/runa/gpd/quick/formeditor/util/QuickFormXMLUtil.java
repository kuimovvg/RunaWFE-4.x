package ru.runa.gpd.quick.formeditor.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.Bundle;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.formeditor.QuickForm;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

public class QuickFormXMLUtil {
    private static final String TEMPLATE_PATH = "/template/";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ELEMENT_FORM = "form";
    public static final String ELEMENT_PARAM = "param";
    public static final String ELEMENT_TAG = "tag";
    public static final String ELEMENT_TAGS = "tags";

    public static String getTemplateFromRegister(Bundle bundle, String templateName) {
        String path = TEMPLATE_PATH + templateName;
        try {
            InputStream is = bundle.getEntry(path).openStream();
            return IOUtils.readStream(is);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read config at " + path, e);
        }
    }

    public static final byte[] convertQuickFormToXML(IFolder folder, QuickForm form, String templateFileName) throws UnsupportedEncodingException, CoreException {
        Document document = XmlUtil.createDocument(ELEMENT_FORM);
        
        saveTemplateToProcessDefinition(folder, form, templateFileName);

        Element tagsElement = document.getRootElement().addElement(ELEMENT_TAGS);
        
        for (QuickFormGpdVariable templatedVariableDef : form.getVariables()) {
            populateQuickFormVariable(tagsElement.addElement(ELEMENT_TAG), templatedVariableDef);
        }

        byte[] bytes = XmlUtil.writeXml(document, OutputFormat.createPrettyPrint());

        return bytes;
    }

    private static void populateQuickFormVariable(Element element, QuickFormGpdVariable templatedVariableDef) {
        element.addElement(ATTRIBUTE_NAME).addText(getNotNullValue(templatedVariableDef.getTagName().toString()));
        element.addElement(ELEMENT_PARAM).addText(templatedVariableDef.getName());
        if (templatedVariableDef.getParams() != null) {
            for (String param : templatedVariableDef.getParams()) {
                element.addElement(ELEMENT_PARAM).addText(param);
            }
        }
    }

    private static String getNotNullValue(String object) {
        return object == null ? "" : object;
    }

    @SuppressWarnings("unchecked")
    public static final QuickForm getQuickFormFromXML(IFile file, ProcessDefinition processDefinition, String templateFileName) {
        QuickForm quickForm = new QuickForm();
        if (file.exists() && getContentLenght(file.getLocation().toString()) != 0) {
            try {
                Document document = XmlUtil.parseWithoutValidation(file.getContents());
                IFile confFile = ((IFolder) file.getParent()).getFile(templateFileName);
                if (confFile.exists()) {
                    String configuration = IOUtils.readStream(confFile.getContents());
                    quickForm.setDelegationConfiguration(configuration);
                }
                Element tagsElement = document.getRootElement().element(ELEMENT_TAGS);
                List<Element> varElementsList = tagsElement.elements(ELEMENT_TAG);
                for (Element varElement : varElementsList) {
                    QuickFormGpdVariable templatedVariableDef = new QuickFormGpdVariable();
                    templatedVariableDef.setTagName(varElement.elementText(ATTRIBUTE_NAME));
                    
                    List<Element> paramElements = varElement.elements(ELEMENT_PARAM);
                    if (paramElements != null && paramElements.size() > 0) {
                        List<String> params = new ArrayList<String>();
                        int index = 0;
                        for (Element paramElement : paramElements) {
                        	if(index == 0) {
                        		templatedVariableDef.setName(paramElement.getText());
                        		Variable variable = VariableUtils.getVariableByName(processDefinition, templatedVariableDef.getName());
                                if (variable == null) {
                                    continue;
                                }
                                templatedVariableDef.setFormatLabel(variable.getFormatLabel());                              
                        	} else {
                        		params.add(paramElement.getText());
                        	}
                            
                            index++;
                        }
                        
                        templatedVariableDef.setParams(params.toArray(new String[0]));
                    }

                    quickForm.getVariables().add(templatedVariableDef);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return quickForm;
    }

    private static void saveTemplateToProcessDefinition(IFolder folder, QuickForm quickForm, String templateFileName) throws CoreException {
        if (!Strings.isNullOrEmpty(quickForm.getDelegationConfiguration())) {
            String configurationFileName = templateFileName;
            IFile configurationFile = folder.getFile(configurationFileName);
            ByteArrayInputStream stream = new ByteArrayInputStream(quickForm.getDelegationConfiguration().getBytes(Charsets.UTF_8));
            if (configurationFile.exists()) {
                configurationFile.setContents(stream, true, true, null);
            } else {
                configurationFile.create(stream, true, null);
            }
        }
    }

    private static final long getContentLenght(String path) {
        File file = new File(path);
        return file.length();
    }
}
