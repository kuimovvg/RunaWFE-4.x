package ru.runa.bp;

import java.util.Collections;
import java.util.List;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Lists;

/**
 * Parameters holder for Alfresco handler.
 * 
 * @author dofs
 */
public class AlfHandlerData extends HandlerData {
    private final List<Long> processIdsHierarchy;
    private final User user;
    private final WfTask task;

    public AlfHandlerData(ParamsDef paramsDef, ExecutionContext context) {
        super(paramsDef, context);
        this.user = null;
        this.task = null;
        processIdsHierarchy = ProcessHierarchyUtils.getProcessIds(context.getProcess().getHierarchyIds());
    }

    public AlfHandlerData(ParamsDef paramsDef, User user, IVariableProvider variableProvider, WfTask task) {
        super(paramsDef, variableProvider, task);
        this.user = user;
        this.task = task;
        WfProcess process = Delegates.getExecutionService().getProcess(user, getProcessId());
        processIdsHierarchy = ProcessHierarchyUtils.getProcessIds(process.getHierarchyIds());
    }

    /**
     * @return subprocess ids hierarchy from root process to current process
     */
    public List<Long> getProcessIdsHierarchy() {
        return processIdsHierarchy;
    }

    /**
     * @return subprocess ids hierarchy from current process to root process
     */
    public List<Long> getProcessIdsHierarchyInversed() {
        List<Long> reversed = Lists.newArrayList(processIdsHierarchy);
        Collections.reverse(reversed);
        return reversed;
    }

    public User getUser() {
        return user;
    }

    public WfTask getTask() {
        return task;
    }

}
