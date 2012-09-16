/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.validators;


import ru.runa.commons.validation.ValidationException;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.InterpreterError;
import bsh.ParseException;
import bsh.TargetError;


public class BSFExpressionValidator extends ValidatorSupport {
    private String expression;

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public void validate() throws ValidationException {
        try {
        	Interpreter interpreter = new Interpreter();

        	for (String varName : validatorContext.getVariables().keySet()) {
        		Object value = validatorContext.getVariables().get(varName);
       			interpreter.set(varName, value);
			}
        	Boolean answer = (Boolean) interpreter.eval(expression);
            if (!answer.booleanValue()) {
    			addActionError();
            }
		} catch (InterpreterError e) {
			throw new ValidationException("BeanShell interpreter internal error: " + e.getMessage());
		} catch (TargetError e) {
			throw new ValidationException("The application script threw an exception: " + e.getMessage());
		} catch (ParseException e) {
			throw new ValidationException("BeanShell script error: " + e.getMessage());
		} catch (EvalError e) {
			// this means that some of the beans are null, we ignoring it due to required validator check
		}
    }
}
