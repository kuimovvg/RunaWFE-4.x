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
package ru.runa.validators;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.commons.validation.Validator;
import ru.runa.commons.validation.ValidatorContext;



public abstract class ValidatorSupport implements Validator {
    protected final Log log = LogFactory.getLog(this.getClass());
    private String type;
    protected String message;
    private Map<String, String> localVariables;
    protected ValidatorContext validatorContext;

    public void init(Map<String, String> parameters) throws Exception {
		this.localVariables = parameters;
		for (String propName : parameters.keySet()) {
			String paramValue = parameters.get(propName);
			BeanUtils.setProperty(this, propName, paramValue);
		}
    }

    @Override
	public Object getParameter(String name) {
		String orig = localVariables.get(name);
		if (orig == null) {
			return null;
		}
		orig = orig.trim();
		if (orig.startsWith("${") && orig.endsWith("}")) {
			// i.e. simple replacement
			String varName = orig.substring(2, orig.length()-2);
			return validatorContext.getVariable(varName);
		}
		return translateVariables('$', orig);
	}

    @Override
	public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        String m = translateVariables('$', message);
        return m.replaceAll("\t", " ").replaceAll("\n", " ").trim();
    }
    
    @Override
    public void setValidatorContext(ValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }

    @Override
    public void setValidatorType(String type) {
        this.type = type;
    }

    @Override
    public String getValidatorType() {
        return type;
    }

    protected void addActionError() {
        validatorContext.addActionError(getMessage());
    }

    // TODO VariablesUtil.substitute(value, variables)
    private String translateVariables(char open, String expression) {
        while (true) {
            int start = expression.indexOf(open + "{");
            int length = expression.length();
            int x = start + 2;
            int end;
            char c;
            int count = 1;
            while (start != -1 && x < length && count != 0) {
                c = expression.charAt(x++);
                if (c == '{') {
                    count++;
                } else if (c == '}') {
                    count--;
                }
            }
            end = x - 1;

            if ((start != -1) && (end != -1) && (count == 0)) {
                String varName = expression.substring(start + 2, end);

                String left = expression.substring(0, start);
                String right = expression.substring(end + 1);

                String varValue = localVariables.get(varName);
                if (varValue == null) {
	                Object var = validatorContext.getVariable(varName);
	                // the variable doesn't exist, so don't display anything
	                varValue = (var == null) ? "null" : var.toString();
                }
               	expression = left + varValue + right;
            } else {
                break;
            }
        }

        return expression;
    }

}
