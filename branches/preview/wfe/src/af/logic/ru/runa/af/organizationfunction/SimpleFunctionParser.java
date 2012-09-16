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
package ru.runa.af.organizationfunction;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 30.11.2005
 * 
 */
public class SimpleFunctionParser implements FunctionParser {

    private static final String functionNameRegexp = "^(\\w+[\\w\\.]*)\\(([^\\)]*)\\)$";

    private SimpleFunctionParser() {
    }

    public static SimpleFunctionParser INSTANCE = new SimpleFunctionParser();

    private final Map<String, FunctionConfiguration> CACHE = new HashMap<String, FunctionConfiguration>();

    public FunctionConfiguration parse(String configuration) throws FunctionParserException {
        if (configuration == null) {
            throw new IllegalArgumentException("Congiguration string is null");
        }
        if (configuration.trim().startsWith("<![CDATA[")) { // TODO remove in future
            configuration = configuration.trim();
            configuration = configuration.substring(9, configuration.length() - 3);
        }
        FunctionConfiguration functionConfiguration = CACHE.get(configuration);
        if (functionConfiguration == null) {
            Pattern functionNamePattern = Pattern.compile(functionNameRegexp);
            Matcher functionNameMatcher = functionNamePattern.matcher(configuration);

            if (!functionNameMatcher.matches()) {
                throw new FunctionParserException("Illegal configuration string", configuration);
            }

            String functionName = functionNameMatcher.group(1);
            if (functionName == null) {
                throw new FunctionParserException("Illegal or missing function name", configuration);
            }

            String parameterString = functionNameMatcher.group(2);
            if (parameterString == null) {
                throw new FunctionParserException("Illegal parameter names", configuration);
            }

            String[] parameters = parameterString.length() == 0 ? new String[0] : parameterString.split(",", -1);

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].length() == 0) {
                    throw new FunctionParserException("Illegal parameter name", configuration);
                }
            }

            functionConfiguration = new FunctionConfiguration(functionName, parameters);
            CACHE.put(configuration, functionConfiguration);
        }
        return functionConfiguration;
    }

//    Commented 10.05.2012. If you see it in 2013 or later - delete commented code.      
//    private static final String ORG_FUNCTION_RESULT_SYMBOL = "?";
//
//    private static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");//    ^\$\{(.*)\}$
//
//    public Object[] getParameters(String[] parameterNames, Map variableMap, Long actorToSubstituteCode) {
//        Object[] parameters = new Object[parameterNames.length];
//        for (int i = 0; i < parameterNames.length; i++) {
//            Matcher matcher = pattern.matcher(parameterNames[i]);
//            if (matcher.matches()) {
//                parameters[i] = variableMap.get(matcher.group(1));
//            } else if (ORG_FUNCTION_RESULT_SYMBOL.equals(parameterNames[i]) && actorToSubstituteCode != null) {
//                parameters[i] = String.valueOf(actorToSubstituteCode.longValue());
//            } else {
//                parameters[i] = parameterNames[i];
//            }
//        }
//        return parameters;
//    }
}
