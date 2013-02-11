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
package ru.runa.af.web.orgfunction;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;

@SuppressWarnings("unchecked")
public class SubstitutionCriteriaDefinitions {
    private static final Log log = LogFactory.getLog(SubstitutionCriteriaDefinitions.class);

    private static final String LIST_XML = "substitutioncriterias.xml";
    private static List<FunctionDef> definitions = new ArrayList<FunctionDef>();

    static {
        try {
            InputStream is = ClassLoaderUtil.getAsStreamNotNull(LIST_XML, SubstitutionDefinitions.class);
            Document document = XmlUtils.parseWithoutValidation(is);
            List<Element> oElements = document.getRootElement().elements("type");
            for (Element oElement : oElements) {
                String className = oElement.attributeValue("class");
                String label = Delegates.getSystemService().getLocalized(null, className);
                FunctionDef fDef = new FunctionDef(className, label);
                List<Element> pElements = oElement.elements("param");
                for (Element pElement : pElements) {
                    String rendererClassName = pElement.attributeValue("renderer");
                    if (rendererClassName == null) {
                        rendererClassName = StringRenderer.class.getName();
                    }
                    ParamRenderer renderer = ClassLoaderUtil.instantiate(rendererClassName);
                    ParamDef pDef = new ParamDef(pElement.attributeValue("messageKey"), pElement.attributeValue("message"), renderer);
                    fDef.addParam(pDef);
                }
                definitions.add(fDef);
            }
        } catch (Exception e) {
            log.error("Check " + LIST_XML, e);
        }
    }

    public static List<FunctionDef> getAll() {
        return definitions;
    }

    public static FunctionDef getByClassName(String className) {
        for (FunctionDef definition : definitions) {
            if (definition.getClassName().equals(className)) {
                return definition;
            }
        }
        return null;
    }
}
