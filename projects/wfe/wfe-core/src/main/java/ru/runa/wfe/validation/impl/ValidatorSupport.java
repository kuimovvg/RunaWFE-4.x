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
package ru.runa.wfe.validation.impl;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.validation.Validator;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.var.MapDelegableVariableProvider;

public abstract class ValidatorSupport implements Validator {
    protected final Log log = LogFactory.getLog(this.getClass());
    private String type;
    protected String message;
    private Map<String, String> localVariables;
    protected ValidatorContext validatorContext;

    @Override
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
        return ExpressionEvaluator.substitute(orig, new MapDelegableVariableProvider(localVariables, validatorContext.getVariableProvider()));
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        String m = ExpressionEvaluator.substitute(message, new MapDelegableVariableProvider(localVariables, validatorContext.getVariableProvider()));
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

}
