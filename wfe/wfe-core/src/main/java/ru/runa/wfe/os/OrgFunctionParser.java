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
package ru.runa.wfe.os;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Preconditions;

public class OrgFunctionParser {

    private static final String functionNameRegexp = "^(\\w+[\\w\\.]*)\\(([^\\)]*)\\)$";

    private OrgFunctionParser() {
    }

    private static final Map<String, FunctionConfiguration> CACHE = new HashMap<String, FunctionConfiguration>();

    public static FunctionConfiguration parse(String configuration) throws OrgFunctionException {
        Preconditions.checkArgument(configuration != null, "Congiguration string is null");
        if (configuration.trim().startsWith("<![CDATA[")) {
            throw new InternalApplicationException("CDATA found at: " + configuration);
        }
        FunctionConfiguration functionConfiguration = CACHE.get(configuration);
        if (functionConfiguration == null) {
            Pattern functionNamePattern = Pattern.compile(functionNameRegexp);
            Matcher functionNameMatcher = functionNamePattern.matcher(configuration);
            if (!functionNameMatcher.matches()) {
                throw new OrgFunctionException("Illegal configuration: " + configuration);
            }
            String functionName = functionNameMatcher.group(1);
            if (functionName == null) {
                throw new OrgFunctionException("Illegal or missing function name in " + configuration);
            }
            String parameterString = functionNameMatcher.group(2);
            if (parameterString == null) {
                throw new OrgFunctionException("Illegal parameter names in " + configuration);
            }
            String[] parameters = parameterString.length() == 0 ? new String[0] : parameterString.split(",", -1);
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].length() == 0) {
                    throw new OrgFunctionException("Illegal parameter name in " + configuration);
                }
            }
            functionConfiguration = new FunctionConfiguration(functionName, parameters);
            CACHE.put(configuration, functionConfiguration);
        }
        return functionConfiguration;
    }

}
