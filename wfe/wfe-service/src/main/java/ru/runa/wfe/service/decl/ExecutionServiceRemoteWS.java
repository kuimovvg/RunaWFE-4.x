package ru.runa.wfe.service.decl;

import java.util.List;

import javax.ejb.Remote;

import ru.runa.wfe.user.User;
import ru.runa.wfe.var.jaxb.WfVariable;

@Remote
public interface ExecutionServiceRemoteWS {

    public List<WfVariable> getVariablesWS(User user, Long processId);

    public Long startProcessWS(User user, String definitionName, List<WfVariable> variables);

    public void completeTaskWS(User user, Long taskId, List<WfVariable> variables, Long swimlaneActorId);

    public void updateVariablesWS(User user, Long processId, List<WfVariable> variables);

}
