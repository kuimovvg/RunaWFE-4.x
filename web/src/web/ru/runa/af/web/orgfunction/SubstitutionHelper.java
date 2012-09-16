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

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;
import ru.runa.af.organizationfunction.FunctionConfiguration;
import ru.runa.af.organizationfunction.ParamRenderer;
import ru.runa.af.organizationfunction.SimpleFunctionParser;

public class SubstitutionHelper {
    private static final Log log = LogFactory.getLog(SubstitutionHelper.class);

    public static String injectFunction(String orgFunction) {
        try {
            return SimpleFunctionParser.INSTANCE.parse(orgFunction).getFunctionName();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public static String injectParameter(String orgFunction, int index) {
        try {
            return SimpleFunctionParser.INSTANCE.parse(orgFunction).getParameters()[index];
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public static String getUserFriendlyOrgFunction(Subject subject, PageContext pageContext, String orgFunction) {
        try {
            StringBuffer result = new StringBuffer();
            FunctionConfiguration configuration = SimpleFunctionParser.INSTANCE.parse(orgFunction);
            String[] parameters = configuration.getParameters();
            FunctionDef functionDef = SubstitutionDefinitions.getByClassName(configuration.getFunctionName());
            result.append(functionDef.getMessage(pageContext));
            result.append("(");
            for (int i = 0; i < functionDef.getParams().size(); i++) {
                if (i != 0) {
                    result.append(", ");
                }
                ParamRenderer renderer = functionDef.getParams().get(i).getRenderer();
                result.append(renderer.getDisplayLabel(subject, parameters[i]));
            }
            result.append(")");
            return result.toString();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }
}
