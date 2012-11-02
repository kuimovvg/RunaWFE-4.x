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
package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;

/**
 * Launch child tokens from the fork over the leaving transitions.
 */
public class Fork extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.Fork;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();
        checkCyclicExecution(token);
        for (Transition leavingTransition : getLeavingTransitions()) {
            Token childToken = new Token(token, leavingTransition.getName());
            ExecutionContext childExecutionContext = new ExecutionContext(executionContext.getProcessDefinition(), childToken);
            leave(childExecutionContext, leavingTransition);
        }
    }

    private void checkCyclicExecution(Token token) {
        int unsavedTokensLevel = 0;
        while (token != null && token.getId() == null) {
            unsavedTokensLevel++;
            token = token.getParent();
        }
        if (unsavedTokensLevel > 100) {
            throw new RuntimeException("Cyclic fork execution does not allowed");
        }
    }

}
