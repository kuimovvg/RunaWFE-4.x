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
package ru.runa.wfe.lang.jpdl;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public class Join extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.Join;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();
        // if this token is not able to reactivate the parent,
        // we don't need to check anything
        if (token.isAbleToReactivateParent()) {
            // the token arrived in the join and can only reactivate
            // the parent once
            token.setAbleToReactivateParent(false);
            Token parentToken = token.getParent();
            boolean reactivateParent = true;
            for (Token childToken : parentToken.getActiveChildren()) {
                if (childToken.isAbleToReactivateParent()) {
                    reactivateParent = false;
                    break;
                }
            }
            if (reactivateParent) {
                // write to all child tokens that the parent is already
                // reactivated
                for (Token childToken : parentToken.getActiveChildren()) {
                    childToken.setAbleToReactivateParent(false);
                }
                // write to all child tokens that the parent is already
                // reactivated
                leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
            }
        }
        token.end(executionContext, null);
    }

}
