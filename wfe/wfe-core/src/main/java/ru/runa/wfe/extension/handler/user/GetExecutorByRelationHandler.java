package ru.runa.wfe.extension.handler.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.relation.dao.RelationPairDAO;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

public class GetExecutorByRelationHandler extends CommonParamBasedHandler {
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private RelationPairDAO relationPairDAO;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String relationName = handlerData.getInputParam(String.class, "relationName");
        Executor right = handlerData.getInputParam(Executor.class, "right");
        List<Executor> executors = Lists.newArrayList(right);
        Relation relation = relationDAO.getNotNull(relationName);
        List<RelationPair> pairs = relationPairDAO.getExecutorsRelationPairsRight(relation, executors);
        if (pairs.size() == 0) {
            if (handlerData.getInputParam(boolean.class, "throwErrorIfMissed")) {
                throw new Exception("Relation '" + relationName + "' does not defined for " + right.getLabel());
            }
            return;
        }
        if (pairs.size() > 1) {
            if (handlerData.getInputParam(boolean.class, "throwErrorIfMultiple")) {
                log.warn(pairs);
                throw new Exception("Relation '" + relationName + "' has multiple choice for " + right.getLabel());
            }
        }
        Executor executor = pairs.get(0).getLeft();
        handlerData.setOutputParam("result", executor);
    }

}
