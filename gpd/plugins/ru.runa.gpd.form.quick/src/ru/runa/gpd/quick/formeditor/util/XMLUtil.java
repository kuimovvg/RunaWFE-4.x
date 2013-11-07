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
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

public class XMLUtil {
	private static final String TEMPLATE = "template";
	private static final String TEMPLATE_NAME = "name";
	private static final String TEMPLATE_VARIABLE = "variables";
	
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_TAG = "tag";
	private static final String ATTRIBUTE_FORMAT = "format";
	private static final String ATTRIBUTE_DESCRIPTION = "description";
	private static final String ATTRIBUTE_PARAM = "param";
	
	public static String getTemplateFromRegister(Bundle bundle, String templateName) {
        String path = "/template/" + templateName;
        try {
            InputStream is = bundle.getEntry(path).openStream();
            return IOUtils.readStream(is);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read config at " + path, e);
        }
    }
	
	public static final byte[] convertTemplateFormToXML(IFolder folder, QuickForm form) throws UnsupportedEncodingException, CoreException {
		Document document = XmlUtil.createDocument(TEMPLATE);
		document.getRootElement().addAttribute(TEMPLATE_NAME, form.getDelegationClassName());
		//save in new file 
		saveTemplateToProcessDefinition(folder, form);
		
		for(QuickFormGpdVariable templatedVariableDef : form.getQuickFormGpdVariable()) {
			populateTemplateVariable(document.getRootElement().addElement(TEMPLATE_VARIABLE), templatedVariableDef);
		}
		
		byte[] bytes = XmlUtil.writeXml(document, OutputFormat.createPrettyPrint());
		
		return bytes;
	}
	
	private static void populateTemplateVariable(Element element, QuickFormGpdVariable templatedVariableDef) {
		element.addElement(ATTRIBUTE_NAME).addText(getNotNullValue(templatedVariableDef.getName()));
		element.addElement(ATTRIBUTE_TAG).addText(getNotNullValue(templatedVariableDef.getTagName().toString()));
		element.addElement(ATTRIBUTE_FORMAT).addText(getNotNullValue(templatedVariableDef.getFormat()));
		element.addElement(ATTRIBUTE_DESCRIPTION).addText(getNotNullValue(templatedVariableDef.getDescription()));
		if(templatedVariableDef.getParams() != null) {
			for(String param : templatedVariableDef.getParams()) {
				element.addElement(ATTRIBUTE_PARAM).addText(param);
			}
		}		
	}
	
	private static String getNotNullValue(String object) {
		return object == null ? "" : object;
	}
	
	public static final QuickForm getTemplateFormFromXML(IFile file, ProcessDefinition processDefinition) throws CoreException {
		QuickForm templateForm = new QuickForm();
		if (file.exists() && getContentLenght(file.getLocation().toString()) != 0) {
			
			Document document = XmlUtil.parseWithoutValidation(file.getContents());
			templateForm.setDelegationClassName(document.getRootElement().attributeValue(TEMPLATE_NAME));			
			
			@SuppressWarnings("unchecked")
			List<Element> varElementsList = document.getRootElement().elements(TEMPLATE_VARIABLE);
			for (Element varElement : varElementsList) {
				QuickFormGpdVariable templatedVariableDef = new QuickFormGpdVariable();		
				templatedVariableDef.setTagName(varElement.elementText(ATTRIBUTE_TAG));
				templatedVariableDef.setName(varElement.elementText(ATTRIBUTE_NAME));
				templatedVariableDef.setFormat(varElement.elementText(ATTRIBUTE_FORMAT));
				Variable var = processDefinition.getVariable(templatedVariableDef.getName(), false);
				templatedVariableDef.setFormatLabel(var.getFormatLabel());
				templatedVariableDef.setJavaClassName(var.getJavaClassName());
				templatedVariableDef.setDescription(varElement.elementText(ATTRIBUTE_DESCRIPTION));
				List<Element> paramElements = varElement.elements(ATTRIBUTE_PARAM);
				if(paramElements != null && paramElements.size() > 0) {
					List<String> params = new ArrayList<String>();
					for(Element paramElement : paramElements) {
						params.add(paramElement.getText());
					}
					templatedVariableDef.setParams(params.toArray(new String[0]));
				}
				
				templateForm.getQuickFormGpdVariable().add(templatedVariableDef);
			}
        }
		
		return templateForm;
	}
	
	private static void saveTemplateToProcessDefinition(IFolder folder, QuickForm templateForm) throws CoreException {
		if (!Strings.isNullOrEmpty(templateForm.getDelegationConfiguration())) {
            String configurationFileName = templateForm.getDelegationClassName();
            IFile configurationFile = folder.getFile(configurationFileName);
            ByteArrayInputStream stream = new ByteArrayInputStream(templateForm.getDelegationConfiguration().getBytes(Charsets.UTF_8));
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
