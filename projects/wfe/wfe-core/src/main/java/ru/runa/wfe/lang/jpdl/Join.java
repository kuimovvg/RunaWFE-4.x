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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;

public class Join extends Node {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Join.class);

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

            // wheck how many tokens already arrived in the join
            // for (Token concurrentToken : parentToken.getChildren().values()) {
            // if (this.equals(concurrentToken.getNode(executionContext.getProcessDefinition()))) {
            // n++;
            // }
            // }

            // check all concurrent tokens and reactivate the parent if the last token arrives in the join
            boolean reactivateParent = mustParentBeReactivated(parentToken, parentToken.getChildren());
            if (reactivateParent) {
                // write to all child tokens that the parent is already
                // reactivated
                for (Token concurrentToken : parentToken.getChildren()) {
                    concurrentToken.setAbleToReactivateParent(false);
                }
                // write to all child tokens that the parent is already reactivated
                leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
            }
        }
        token.end(executionContext, false);
    }

    private boolean mustParentBeReactivated(Token parentToken, Collection<Token> childTokens) {
        boolean reactivateParent = true;
        for (Token token : childTokens) {
            if (token != null && token.isAbleToReactivateParent()) {
                log.debug("join will not yet reactivate parent: found concurrent token '" + token + "'");
                reactivateParent = false;
                break;
            }
        }
        return reactivateParent;
    }

}
