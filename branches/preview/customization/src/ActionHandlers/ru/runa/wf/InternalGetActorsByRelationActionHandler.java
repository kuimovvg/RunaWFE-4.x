package ru.runa.wf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.ConfigurationException;
import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.RelationPair;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.RelationDAO;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.node.MultiInstanceState;

public class InternalGetActorsByRelationActionHandler implements ActionHandler {
    private String name;
    private String param;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private RelationDAO relationDAO;

    private List<Actor> getActorsByRelation(String relationName, String relationParam) {
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        List<Executor> executorRightList = new ArrayList<Executor>();
        try {
            Actor actor = executorDAO.getActor(relationParam);
            executorRightList.add(actor);
            executorRightList.addAll(executorDAO.getExecutorGroups(actor, batchPresentation));
        } catch (Exception ex) {
            try {
                Group group = executorDAO.getGroup(relationParam);
                executorRightList.add(group);
            } catch (Exception e) {
                throw new InternalApplicationException(e);
            }
        }
        try {
            List<RelationPair> relationPairList = relationDAO.getExecutorsRelationPairsRight(relationName, executorRightList);
            Set<Actor> actorList = new HashSet<Actor>();
            for (int i = 0; i < relationPairList.size(); i++) {
                Executor executorleft = relationPairList.get(i).getLeft();
                if (executorleft instanceof Actor) {
                    actorList.add((Actor) executorleft);
                } else if (executorleft instanceof Group) {
                    actorList.addAll(executorDAO.getGroupActors((Group) executorleft));
                }
            }
            return new ArrayList<Actor>(actorList);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void execute(ExecutionContext context) {
        List<Actor> actors = getActorsByRelation(name, param);
        List<String> actorCodes = new ArrayList<String>(actors.size());
        for (Actor actor : actors) {
            actorCodes.add(String.valueOf(actor.getCode()));
        }
        context.setTransientVariable(MultiInstanceState.OUT_VARIABLE_NAME, actorCodes);
    }

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        try {
            Element root = DocumentHelper.parseText(configuration).getRootElement();
            this.name = root.attributeValue("name");
            if (this.name == null) {
                this.name = root.elementTextTrim("name");
            }
            this.param = root.attributeValue("param");
            if (this.param == null) {
                this.param = root.elementTextTrim("param");
            }
        } catch (DocumentException e) {
            throw new ConfigurationException("Invalid XML for " + getClass(), e);
        }
    }
}
