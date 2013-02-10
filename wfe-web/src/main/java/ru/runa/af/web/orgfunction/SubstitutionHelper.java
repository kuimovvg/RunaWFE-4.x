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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.os.OrgFunction;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.os.ParamRenderer;
import ru.runa.wfe.user.User;

public class SubstitutionHelper {
    private static final Log log = LogFactory.getLog(SubstitutionHelper.class);

    public static String injectFunction(String swimlaneInitializer) {
        try {
            return OrgFunctionHelper.parseOrgFunction(swimlaneInitializer).getClass().getName();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public static String injectParameter(String swimlaneInitializer, int index) {
        try {
            return OrgFunctionHelper.parseOrgFunction(swimlaneInitializer).getParameterNames()[index];
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public static String getUserFriendlyOrgFunction(User user, String swimlaneInitializer) {
        StringBuffer result = new StringBuffer();
        OrgFunction function = OrgFunctionHelper.parseOrgFunction(swimlaneInitializer);
        FunctionDef functionDef = SubstitutionDefinitions.getByClassNameNotNull(function.getClass().getName());
        result.append(functionDef.getLabel());
        result.append("(");
        for (int i = 0; i < functionDef.getParams().size(); i++) {
            if (i != 0) {
                result.append(", ");
            }
            ParamRenderer renderer = functionDef.getParams().get(i).getRenderer();
            result.append(renderer.getDisplayLabel(user, function.getParameterNames()[i]));
        }
        result.append(")");
        return result.toString();
    }
}
