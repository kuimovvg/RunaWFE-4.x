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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
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
    
    public static Object parseJSON(VariableFormat format, Object source) throws Exception {
        // TODO VariableFormat.json; parse and format move here
        if (source instanceof JSONObject) {
            if (format instanceof UserTypeFormat) {
                return ((UserTypeFormat) format).parse((JSONObject) source);
            }
            if (format instanceof MapFormat) {
                // TODO ComplexVariable as for list
                return Maps.newHashMap((JSONObject) source);
            }
            throw new InternalApplicationException("JSONObject is not expected for " + format);
        }
        if (source instanceof JSONArray) {
            JSONArray array = (JSONArray) source;
            if (format instanceof ListFormat) {
                List<Object> result = Lists.newArrayListWithExpectedSize(array.size());
                VariableFormat componentFormat = FormatCommons.createComponent((VariableFormatContainer) format, 0);
                for (Object object : array) {
                    try {
                        if (object instanceof String) {
                            result.add(componentFormat.parse((String) object));
                        } else {
                            result.add(TypeConversionUtil.convertTo(componentFormat.getJavaClass(), object));
                        }
                    } catch (Exception e) {
                        log.warn("Value = " + object, e);
                        result.add(null);
                    }
                }
                return result;
            }
            throw new InternalApplicationException("JSONArray is not expected for " + format);
        }
        return source;
    }

}
