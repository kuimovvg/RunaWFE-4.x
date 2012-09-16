/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.context.exe;

import java.util.HashMap;
import java.util.Map;

import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.module.exe.ModuleInstance;

import com.google.common.collect.Maps;

/**
 * maintains all the key-variable pairs for a process instance.
 */
public class ContextInstance extends ModuleInstance {
    private static final long serialVersionUID = 1L;

    private Map<Token, TokenVariableMap> tokenVariableMaps = Maps.newHashMap();

    /**
     * gets all the variableInstances on the root-token (= process-instance
     * scope).
     */
    public Map<String, Object> getVariables() {
        return getVariables(getRootToken());
    }

    /**
     * retrieves all the variableInstances in scope of the given token.
     */
    private Map<String, Object> getVariables(Token token) {
        TokenVariableMap tokenVariableMap = getTokenVariableMap(token);
        if (tokenVariableMap != null) {
            return tokenVariableMap.getVariables();
        }
        return new HashMap<String, Object>();
    }

    /**
     * adds all the variableInstances on the root-token (= process-instance
     * scope).
     */
    public void setVariables(ExecutionContext executionContext, Map<String, Object> variables) {
        setVariables(executionContext, variables, getRootToken());
    }

    /**
     * adds all the variableInstances to the scope of the given token. The
     * method setVariables is the same as the {@link #addVariables(Map, Token)},
     * but it was added for more consistency.
     */
    public void setVariables(ExecutionContext executionContext, Map<String, Object> variables, Token token) {
        // [JBPM-1778] Empty map variables on process creation is set as null
        TokenVariableMap tokenVariableMap = getOrCreateTokenVariableMap(token);
        tokenVariableMap.setVariables(executionContext, variables);
    }

    /**
     * gets the variable with the given name on the root-token (=
     * process-instance scope).
     */
    public Object getVariable(String name) {
        return getVariable(name, getRootToken());
    }

    /**
     * retrieves a variable in the scope of the token. If the given token does
     * not have a variable for the given name, the variable is searched for up
     * the token hierarchy.
     */
    public Object getVariable(String name, Token token) {
        TokenVariableMap tokenVariableMap = getTokenVariableMap(token);
        if (tokenVariableMap != null) {
            return tokenVariableMap.getVariable(name);
        }
        return null;
    }

    /**
     * retrieves a variable which is local to the token. this method was added
     * for naming consistency. it is the same as
     * {@link #getLocalVariable(String, Token)}.
     */
    public Object getLocalVariable(String name, Token token) {
        TokenVariableMap tokenVariableMap = tokenVariableMaps.get(token);
        if (tokenVariableMap != null) {
            return tokenVariableMap.getLocalVariable(name);
        }
        return null;
    }

    /**
     * sets a variable on the process instance scope.
     */
    public void setVariable(ExecutionContext executionContext, String name, Object value) {
        setVariable(executionContext, name, value, getRootToken());
    }

    /**
     * sets a variable. If a variable exists in the scope given by the token,
     * that variable is updated. Otherwise, the variable is created on the root
     * token (=process instance scope).
     */
    public void setVariable(ExecutionContext executionContext, String name, Object value, Token token) {
        TokenVariableMap tokenVariableMap = getOrCreateTokenVariableMap(token);
        tokenVariableMap.setVariable(executionContext, name, value);
    }

    /**
     * deletes the given variable on the root-token (=process-instance scope).
     */
    public void deleteVariable(String name) {
        deleteVariable(name, getRootToken());
    }

    /**
     * deletes a variable from the given token. For safety reasons, this method
     * does not propagate the deletion to parent tokens in case the given token
     * does not contain the variable.
     */
    public void deleteVariable(String name, Token token) {
        TokenVariableMap tokenVariableMap = getTokenVariableMap(token);
        if (tokenVariableMap != null) {
            tokenVariableMap.deleteVariable(name);
        }
    }

    private Token getRootToken() {
        return processInstance.getRootToken();
    }

    /*
     * searches for the first token-variable-map for the given token and creates
     * it on the root token if it doesn't exist.
     */
    public TokenVariableMap getOrCreateTokenVariableMap(Token token) {
        if (tokenVariableMaps.containsKey(token)) {
            return tokenVariableMaps.get(token);
        } else if (!token.isRoot()) {
            return getOrCreateTokenVariableMap(token.getParent());
        } else {
            TokenVariableMap tokenVariableMap = new TokenVariableMap(token, this);
            tokenVariableMaps.put(token, tokenVariableMap);
            return tokenVariableMap;
        }
    }

    /*
     * looks for the first token-variable-map that is found up the token-parent
     * hirarchy.
     */
    public TokenVariableMap getTokenVariableMap(Token token) {
        if (tokenVariableMaps.containsKey(token)) {
            return tokenVariableMaps.get(token);
        } else if (!token.isRoot()) {
            return getTokenVariableMap(token.getParent());
        }
        return null;
    }

}
