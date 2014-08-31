/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package ru.runa.af.web.tag;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.apache.struts.Globals;
import org.apache.struts.taglib.TagUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.af.web.action.SavePropertiesAction;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.PropertiesFileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.handler.var.FormulaActionHandlerOperations;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author: petrmikheev Date: 26.08.2012
 * @jsp.tag name = "editProperties" body-content = "JSP"
 */

public class EditPropertiesTag extends TitledFormTag {
    private static final long serialVersionUID = -426375016105456L;
    private static final Log log = LogFactory.getLog(EditPropertiesTag.class);

    private static class Property {
    	public String title;
    	public String pattern = null;
    	public List<String> values = new LinkedList<String>();
    	public Property(String title) {
    		this.title = title;
    	}
    }
    
    private static class PropertiesFile {
    	Property defaultProperty = new Property(null);
    	List<Property> properties = new ArrayList<Property>();
    }
    
    public static TreeMap<String, PropertiesFile> settingsList = new TreeMap<String, PropertiesFile>();

    static {
        readSettingsList("settingsList.xml");
    }

    @SuppressWarnings("unchecked")
	private static void parsePropertyType(Property p, Element el) {
    	p.pattern = el.attributeValue("pattern");
    	List<Element> vlist = el.elements();
    	for (Element v : vlist) {
    		if (v.getName() != "value") continue;
    		p.values.add(v.getText());
    	}
    }
    
    @SuppressWarnings("unchecked")
	private static void readSettingsList(String path) {
        try {
            InputStream is = ClassLoaderUtil.getAsStreamNotNull(path, FormulaActionHandlerOperations.class);
            Document document = XmlUtils.parseWithoutValidation(is);
            List<Element> files = document.getRootElement().elements();
            for (Element f : files) {
            	PropertiesFile pf = new PropertiesFile();
            	settingsList.put(f.attributeValue("title"), pf);
            	parsePropertyType(pf.defaultProperty, f);
            	List<Element> plist = f.elements();
            	for (Element p : plist) {
            		if (p.getName() != "property") continue;
            		Property np = new Property(p.attributeValue("title"));
            		parsePropertyType(np, p);
            		pf.properties.add(np);
            	}
            }
        } catch (Exception e) {
            log.error("Can`t parse " + path, e);
        }
    }
    
    private String resource;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    @Override
    protected String getFormButtonName() {
    	return Messages.getMessage(Messages.BUTTON_SAVE, pageContext);
    }
    
    @Override
    protected void fillFormElement(TD tdFormElement) {
    	if (!Delegates.getExecutorService().isAdministrator(getUser()))
    		throw new AuthorizationException("No permission on this page");
    	if (!settingsList.containsKey(resource))
    		throw new IllegalArgumentException();
    	getForm().setMethod(Form.POST);
    	tdFormElement.addElement(new Input(Input.hidden, PropertiesFileForm.RESOURCE_INPUT_NAME, resource));
    	tdFormElement.addElement(new Input(Input.hidden, "saveButtonText", Messages.getMessage(Messages.BUTTON_SAVE, pageContext)));
    	PropertiesFile pf = settingsList.get(resource);
    	PropertyResources properties = new PropertyResources(resource);
    	Table table = new Table();
    	table.setClass("list");
    	String header_title = Messages.getMessage(Messages.LABEL_SETTING_TITLE, pageContext);
    	String header_description = Messages.getMessage(Messages.LABEL_SETTING_DESCRIPTION, pageContext);
    	String header_value = Messages.getMessage(Messages.LABEL_SETTING_VALUE, pageContext);
    	table.addElement("<tr><th class='list'>"+header_title+"</th><th class='list'>"+header_description+
    			"</th><th class='list' style='width:300px'>"+header_value+"</th></tr>");
    	List<Property> lp = pf.properties;
    	if (lp.size() == 0) {
    		lp = new LinkedList<Property>();
    		for (Object k : properties.getAllPropertyNames()) {
    			Property p = new Property(k.toString());
    			p.pattern = pf.defaultProperty.pattern;
    			p.values = pf.defaultProperty.values;
    			lp.add(p);
    		}
    	}
    	for (Property p : lp) {
    		String description = getDescription(pageContext, resource + "_" + p.title);
    		String value = properties.getStringProperty(p.title, "");
    		Input old_input = new Input(Input.hidden, PropertiesFileForm.OLD_VALUE_INPUT_NAME(p.title), value);
    		String input;
    		if (p.values.size() == 0) {
    			Input i = new Input(Input.text, PropertiesFileForm.NEW_VALUE_INPUT_NAME(p.title), value);
    			i.addAttribute("style", "width: 290px");
    			if (p.pattern != null) i.addAttribute("pattern", p.pattern);
    			input = i.toString();
    		} else {
    			StringBuilder b = new StringBuilder();
    			b.append("<select style='width: 300px' name='" + PropertiesFileForm.NEW_VALUE_INPUT_NAME(p.title) + "'>");
    			b.append("<option selected>");
    			b.append(value);
    			b.append("</option>");
    			for (String v : p.values) {
    				if (v.equals(value)) continue;
    				b.append("<option>");
    				b.append(v);
    				b.append("</option>");
    			}
    			b.append("</select>");
    			input = b.toString();
    		}
    		table.addElement("<tr><td class='list'>"+p.title+"</td>" +
    						     "<td class='list'>"+description+"</td>" +
    				             "<td class='list'>"+old_input.toString()+input+"</td>" +
    						 "</tr>");
    	}
    	tdFormElement.addElement(table);
    }

    public static String getDescription(PageContext pageContext, String key) {
    	try {
			String res = TagUtils.getInstance().message(pageContext, "settingsDescriptions", Globals.LOCALE_KEY, key);
			if (res == null) res = "";
			return res;
		} catch (JspException e) {
			e.printStackTrace();
			return key; 
		}
    }
    
    @Override
    protected String getTitle() {
        return getDescription(pageContext, resource);
    }

    @Override
    public String getAction() {
    	return SavePropertiesAction.SAVE_PROPERTIES_ACTION_PATH;
    }
    @Override
    protected boolean isCancelButtonEnabled() {
    	return true;
    }
    @Override
    protected String getCancelButtonAction() {
    	return "manage_settings.do";
    }

}
