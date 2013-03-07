package ru.runa.wfe.validation.impl;

import groovy.lang.MissingPropertyException;
import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.IScriptExecutor;
import ru.runa.wfe.validation.Validator;

public class GroovyExpressionValidator extends Validator {
	
	protected IScriptExecutor getScriptExecutor() {
		return new GroovyScriptExecutor();
	}

    @Override
    public void validate() {
        try {
            IScriptExecutor scriptExecutor = getScriptExecutor();
            String expression = getParameterNotNull(String.class, "expression");
            Boolean valid = scriptExecutor.evaluateScript(expression, getVariableProvider());
            if (valid == null || !valid) {
                addError();
            }
        } catch (MissingPropertyException e) {
            // this means that some of the beans are null, we ignoring it due to
            // required validator check
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error("Groovy", e);
            addError();
            // This is because calling side has not Groovy generated classes and
            // will unable to show exception
        }
    }

}
