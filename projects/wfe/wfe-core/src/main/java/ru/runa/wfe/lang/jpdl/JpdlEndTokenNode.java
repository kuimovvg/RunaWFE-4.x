package ru.runa.wfe.lang.jpdl;

import java.util.Set;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.EndTokenNode;
import ru.runa.wfe.lang.NodeType;

import com.google.common.collect.Sets;

public class JpdlEndTokenNode extends EndTokenNode {
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(ExecutionContext executionContext) {
        super.execute(executionContext);
        // If this token was forked
        Token parentToken = executionContext.getToken().getParent();
        if (parentToken != null && parentToken.getNodeType() == NodeType.FORK && parentToken.getActiveChildren().size() == 0) {
            Set<Join> joins = Sets.newHashSet();
            for (Token childToken : parentToken.getChildren()) {
                if (childToken.getNodeType() == NodeType.JOIN) {
                    joins.add((Join) childToken.getNode(executionContext.getProcessDefinition()));
                }
            }
            for (Join join : joins) {
                join.execute(executionContext);
            }
        }
    }
}
