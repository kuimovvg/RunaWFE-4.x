package ru.runa.wfe.validation.impl;

import groovy.lang.MissingPropertyException;
import ru.runa.wfe.commons.GroovyScriptExecutor;

public class GroovyExpressionValidator extends ValidatorSupport {
    private String expression;

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public void validate() throws Exception {
        try {
            GroovyScriptExecutor scriptExecutor = new GroovyScriptExecutor();
            Boolean valid = scriptExecutor.evaluateScript(expression, validatorContext.getVariableProvider());
            if (valid == null || !valid) {
                addActionError();
            }
        } catch (MissingPropertyException e) {
            // this means that some of the beans are null, we ignoring it due to
            // required validator check
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error("Groovy", e);
            addActionError();
            // This is because calling side has not Groovy generated classes and
            // will unable to show exception
        }
    }

}
