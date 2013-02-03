package ru.runa.bp;

import java.util.List;

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.handler.HandlerData;
import ru.runa.wfe.handler.ParamsDef;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Parameters holder for Alfresco handler.
 * 
 * @author dofs
 */
public class AlfHandlerData extends HandlerData {
    private final List<Long> processIdsHierarchy;

    public AlfHandlerData(ParamsDef paramsDef, ExecutionContext context) {
        super(paramsDef, context);
        processIdsHierarchy = ProcessHierarchyUtils.getProcessIds(context.getProcess().getHierarchySubProcess());
    }

    public AlfHandlerData(ParamsDef paramsDef, User user, IVariableProvider variableProvider, WfTask task) {
        super(paramsDef, variableProvider, task);
        WfProcess process = Delegates.getExecutionService().getProcess(user, getProcessId());
        processIdsHierarchy = ProcessHierarchyUtils.getProcessIds(process.getHierarchySubProcess());
    }

    public List<Long> getProcessIdsHierarchy() {
        return processIdsHierarchy;
    }

}
