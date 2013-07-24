package ru.runa.wfe.lang.bpmn2;

import java.util.List;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class Join extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.JOIN;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();
        List<Token> arrivedTokens = Lists.newArrayList();
        fillArrivedInThisNodeTokensWhichCanActivateParent(executionContext.getProcess().getRootToken(), arrivedTokens);
        if (!arrivedTokens.contains(token)) {
            arrivedTokens.add(token);
        }
        List<Token> tokensToPop = Lists.newArrayList();
        boolean allArrivedTransitionArePassed = true;
        for (Transition arrivingTransition : getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (Token arrivedToken : arrivedTokens) {
                if (arrivingTransition.getNodeId().equals(arrivedToken.getTransitionId())) {
                    transitionIsPassedByToken = true;
                    tokensToPop.add(arrivedToken);
                    break;
                }
            }
            if (!transitionIsPassedByToken) {
                allArrivedTransitionArePassed = false;
                break;
            }
        }

        if (allArrivedTransitionArePassed) {
            for (Token arrivedToken : tokensToPop) {
                arrivedToken.setAbleToReactivateParent(false);
            }
            Token parentToken = token.getParent(); // got first parent
            leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
        }

        token.end(executionContext, null);
    }

    private void fillArrivedInThisNodeTokensWhichCanActivateParent(Token parent, List<Token> tokens) {
        if (parent.isAbleToReactivateParent() && Objects.equal(parent.getNodeId(), getNodeId())) {
            tokens.add(parent);
        }
        for (Token childToken : parent.getChildren()) {
            fillArrivedInThisNodeTokensWhichCanActivateParent(childToken, tokens);
        }
    }
}
