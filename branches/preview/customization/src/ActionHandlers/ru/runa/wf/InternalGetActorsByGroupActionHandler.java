package ru.runa.wf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.ConfigurationException;
import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.Group;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.node.MultiInstanceState;

public class InternalGetActorsByGroupActionHandler implements ActionHandler {
    private String name;
    @Autowired
    private ExecutorDAO executorDAO;

    private Set<Actor> getActorsByGroup(String groupName) {
        try {
            Group group = executorDAO.getGroup(groupName);
            return executorDAO.getGroupActors(group);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void execute(ExecutionContext context) {
        Set<Actor> actors = getActorsByGroup(name);
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
        } catch (DocumentException e) {
            throw new ConfigurationException("Invalid XML for " + getClass(), e);
        }
    }
}
