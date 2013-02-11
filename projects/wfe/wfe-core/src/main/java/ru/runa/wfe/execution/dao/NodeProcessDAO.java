package ru.runa.wfe.execution.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class NodeProcessDAO extends GenericDAO<NodeProcess> {

    public NodeProcess getNodeProcessByChild(Long processId) {
        return findFirstOrNull("from NodeProcess where subProcess.id = ?", processId);
    }

    public List<NodeProcess> getNodeProcesses(Long processId) {
        return getHibernateTemplate().find("from NodeProcess where process.id = ? order by id", processId);
    }

    public void deleteByProcess(Process process) {
        log.debug("deleting subprocess nodes for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from NodeProcess where process=?", process);
    }

    public List<Process> getSubprocesses(Process process) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(process.getId());
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    public List<Process> getSubprocessesRecursive(Process process) {
        List<Process> result = getSubprocesses(process);
        for (Process subprocess : result) {
            result.addAll(getSubprocessesRecursive(subprocess));
        }
        return result;
    }

}
