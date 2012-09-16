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
package ru.runa.bpm.graph.node;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.hibernate.LockMode;
import org.hibernate.Session;

import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;
import ru.runa.bpm.par.InvalidProcessDefinition;
import ru.runa.commons.ApplicationContextFactory;

public class Join extends Node implements Parsable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Join.class);

    /**
     * specifies wether what type of hibernate lock should be acquired. null
     * value defaults to LockMode.FORCE
     */
    private String parentLockMode;

    /**
     * specifies if this joinhandler is a discriminator. a descriminator
     * reactivates the parent when the first concurrent token enters the join.
     */
    private boolean discriminator = false;

    /**
     * a fixed set of concurrent tokens. TODO always null
     */
    private Collection<String> tokenNames = null;

    /**
     * reactivate the parent if the n-th token arrives in the join.
     */
    private int nOutOfM = -1;

    @Override
    public NodeType getNodeType() {
        return NodeType.Join;
    }

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element element, JpdlXmlReader jpdlReader) {
        String lock = element.attributeValue("lock");
        if (lock != null) {
            LockMode lockMode = LockMode.parse(lock);
            if (lockMode != null) {
                parentLockMode = lockMode.toString();
            } else if ("pessimistic".equals(lock)) {
                parentLockMode = LockMode.UPGRADE.toString();
            } else {
                throw new InvalidProcessDefinition("invalid parent lock mode '" + lock + "'");
            }
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();

        boolean isAbleToReactivateParent = token.isAbleToReactivateParent();

        if (!token.hasEnded()) {
            token.end(executionContext, false);
        }

        // if this token is not able to reactivate the parent,
        // we don't need to check anything
        if (isAbleToReactivateParent) {

            // the token arrived in the join and can only reactivate
            // the parent once
            token.setAbleToReactivateParent(false);

            Token parentToken = token.getParent();

            if (parentToken != null) {
                Session session = ApplicationContextFactory.getCurrentSession();
                if (session != null) {
                    // force version increment by default (LockMode.FORCE)
                    LockMode lockMode = parentLockMode != null ? LockMode.parse(parentLockMode) : LockMode.FORCE;
                    log.debug("acquiring " + lockMode + " lock on " + parentToken);
                    // lock updates as appropriate, no need to flush here
                    session.lock(parentToken, lockMode);
                }

                boolean reactivateParent = true;

                // if this is a discriminator
                if (discriminator) {
                    // reactivate the parent when the first token arrives in the
                    // join.
                    // this must be the first token arriving, otherwise
                    // isAbleToReactivateParent()
                    // should have been false above.
                    reactivateParent = true;

                    // if a fixed set of tokenNames is specified at design
                    // time...
                } else if (tokenNames != null) {
                    // check reactivation on the basis of those tokenNames
                    reactivateParent = mustParentBeReactivated(parentToken, tokenNames);
                } else if (nOutOfM != -1) {
                    int n = 0;
                    // wheck how many tokens already arrived in the join
                    for (Token concurrentToken : parentToken.getChildren().values()) {
                        if (this.equals(concurrentToken.getNode())) {
                            n++;
                        }
                    }
                    if (n < nOutOfM) {
                        reactivateParent = false;
                    }
                } else {
                    // the default behaviour is to check all concurrent tokens
                    // and reactivate
                    // the parent if the last token arrives in the join
                    reactivateParent = mustParentBeReactivated(parentToken, parentToken.getChildren().keySet());
                }

                // if the parent token needs to be reactivated from this join
                // node
                if (reactivateParent) {
                    // write to all child tokens that the parent is already
                    // reactivated
                    for (Token concurrentToken : parentToken.getChildren().values()) {
                        concurrentToken.setAbleToReactivateParent(false);
                    }
                    // write to all child tokens that the parent is already
                    // reactivated
                    leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
                }
            }
        }
    }

    private boolean mustParentBeReactivated(Token parentToken, Collection<String> childTokenNames) {
        boolean reactivateParent = true;
        for (String concurrentTokenName : childTokenNames) {
            Token concurrentToken = parentToken.getChild(concurrentTokenName);
            if (concurrentToken.isAbleToReactivateParent()) {
                log.debug("join will not yet reactivate parent: found concurrent token '" + concurrentToken + "'");
                reactivateParent = false;
                break;
            }
        }
        return reactivateParent;
    }

    public String getParentLockMode() {
        return parentLockMode;
    }

    public void setParentLockMode(String parentLockMode) {
        this.parentLockMode = parentLockMode;
    }

}
