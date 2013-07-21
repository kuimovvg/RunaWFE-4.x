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

public class GetExecutorsByRelationHandler extends CommonParamBasedHandler {
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private RelationPairDAO relationPairDAO;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String relationName = handlerData.getInputParam(String.class, "name");
        Executor parameter = handlerData.getInputParam(Executor.class, "parameter");
        boolean inversed = handlerData.getInputParam(boolean.class, "inversed");
        List<Executor> executors = Lists.newArrayList(parameter);
        Relation relation = relationDAO.getNotNull(relationName);
        List<RelationPair> pairs;
        if (inversed) {
            pairs = relationPairDAO.getExecutorsRelationPairsLeft(relation, executors);
        } else {
            pairs = relationPairDAO.getExecutorsRelationPairsRight(relation, executors);
        }
        List<Executor> result = Lists.newArrayList();
        for (RelationPair pair : pairs) {
            if (inversed) {
                result.add(pair.getRight());
            } else {
                result.add(pair.getLeft());
            }
        }
        handlerData.setOutputParam("result", result);
    }

}
