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

package ru.runa.wfe.var.format;

import java.util.Map;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;
import ru.runa.wfe.var.dto.WfVariable;

public class FormatCommons {

    private static VariableFormat create(String className, Map<String, VariableUserType> userTypes) {
        if (userTypes != null && userTypes.containsKey(className)) {
            return new UserTypeFormat(userTypes.get(className));
        }
        VariableFormat format = ClassLoaderUtil.instantiate(className);
        if (format instanceof VariableFormatContainer) {
            ((VariableFormatContainer) format).setUserTypes(userTypes);
        }
        return format;
    }

    public static VariableFormat create(VariableDefinition variableDefinition) {
        VariableFormat format = create(variableDefinition.getFormatClassName(), variableDefinition.getUserTypes());
        if (format instanceof VariableFormatContainer) {
            ((VariableFormatContainer) format).setComponentClassNames(variableDefinition.getFormatComponentClassNames());
        }
        return format;
    }

    public static VariableFormat createComponent(VariableFormatContainer formatContainer, int index) {
        String elementFormatClassName = formatContainer.getComponentClassName(index);
        return create(elementFormatClassName, formatContainer.getUserTypes());
    }

    public static VariableFormat createComponent(WfVariable containerVariable, int index) {
        return createComponent((VariableFormatContainer) containerVariable.getDefinition().getFormatNotNull(), index);
    }

    public static String formatComponentValue(VariableFormatContainer formatContainer, int index, Object value) {
        return createComponent(formatContainer, index).format(value);
    }

    public static String formatComponentValue(WfVariable containerVariable, int index, Object value) {
        return createComponent(containerVariable, index).format(value);
    }

    public static VariableUserType getComponentUserType(VariableDefinition variableDefinition, int index) {
        return variableDefinition.getUserTypes().get(variableDefinition.getFormatComponentClassNames()[index]);
    }
}
