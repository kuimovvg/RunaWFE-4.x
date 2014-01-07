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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableDefinitionAware;
import ru.runa.wfe.var.dto.WfVariable;

public class FormatCommons {
    private static final Log log = LogFactory.getLog(FormatCommons.class);

    private static VariableFormat create(String className, VariableDefinition variableDefinition) {
        VariableFormat format = ClassLoaderUtil.instantiate(className);
        if (format instanceof VariableDefinitionAware) {
            ((VariableDefinitionAware) format).setVariableDefinition(variableDefinition);
        }
        return format;
    }

    public static VariableFormat create(VariableDefinition variableDefinition) {
        VariableFormat format = create(variableDefinition.getFormatClassName(), variableDefinition);
        if (format instanceof VariableFormatContainer) {
            ((VariableFormatContainer) format).setComponentClassNames(variableDefinition.getFormatComponentClassNames());
        }
        return format;
    }

    public static VariableFormat createComponent(VariableFormatContainer formatContainer, int index) {
        String elementFormatClassName = formatContainer.getComponentClassName(index);
        VariableDefinition variableDefinition = null;
        if (formatContainer instanceof VariableDefinitionAware) {
            variableDefinition = ((VariableDefinitionAware) formatContainer).getVariableDefinition();
        }
        return create(elementFormatClassName, variableDefinition);
    }

    public static VariableFormat createComponent(WfVariable containerVariable, int index) {
        return createComponent((VariableFormatContainer) containerVariable.getFormatNotNull(), index);
    }

    public static String formatComponentValue(VariableFormatContainer formatContainer, int index, Object value) {
        return createComponent(formatContainer, index).format(value);
    }
    
    public static String getFileOutput(WebHelper webHelper, Long processId, String variableName, FileVariable value) {
        return getFileOutput(webHelper, processId, variableName, value, null, null);
    }

    public static String getFileOutput(WebHelper webHelper, Long processId, String variableName, FileVariable value, Integer listIndex, Object mapKey) {
        if (value == null) {
            return "&nbsp;";
        }
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("id", processId);
        params.put("variableName", variableName);
        if (listIndex != null) {
            params.put("listIndex", String.valueOf(listIndex));
        }
        if (mapKey != null) {
            params.put("mapKey", String.valueOf(mapKey));
        }
        return getFileOutput(webHelper, params, value.getName());
    }

    public static String getFileOutput(WebHelper webHelper, Map<String, Object> params, String fileName) {
        String href = webHelper.getActionUrl("/variableDownloader", params);
        return "<a href=\"" + href + "\">" + fileName + "</>";
    }

}
