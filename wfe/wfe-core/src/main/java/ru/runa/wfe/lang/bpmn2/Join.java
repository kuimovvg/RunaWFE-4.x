package ru.runa.wfe.lang.bpmn2;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.collect.Lists;

public class Join extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.Join;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token token = executionContext.getToken();

        Session session = ApplicationContextFactory.getCurrentSession();
        Query query = session.createQuery("from Token where process=? and nodeId=? and ableToReactivateParent=true");
        query.setParameter(0, token.getProcess());
        query.setParameter(1, getNodeId());
        List<Token> arrivedTokens = query.list();
        arrivedTokens.add(token);

        List<Token> tokenSet = Lists.newArrayList();
        boolean allArrivedTransitionArePassed = true;
        for (Transition arrivingTransition : getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (Token arrivedToken : arrivedTokens) {
                if (arrivingTransition.getNodeId().equals(arrivedToken.getTransitionId())) {
                    transitionIsPassedByToken = true;
                    tokenSet.add(arrivedToken);
                    break;
                }
            }
            if (!transitionIsPassedByToken) {
                allArrivedTransitionArePassed = false;
                break;
            }
        }

        if (allArrivedTransitionArePassed) {
            for (Token arrivedToken : tokenSet) {
                arrivedToken.setAbleToReactivateParent(false);
            }
            Token parentToken = token.getParent(); // got first parent
            leave(new ExecutionContext(executionContext.getProcessDefinition(), parentToken));
        }

        token.end(executionContext, null);
    }

}
