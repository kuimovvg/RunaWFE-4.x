package ru.runa.wfe.graph.image;

import java.util.List;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessesGraphElementAdapter;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.User;

import com.google.common.base.Objects;

/**
 * Operation to add identities of started subprocesses to graph elements.
 */
public class StartedSubprocessesVisitor extends SubprocessesGraphElementAdapter {

    /**
     * Current subject.
     */
    private final User user;

    /**
     * Instances of subprocesses, which must be added to graph elements.
     */
    private final List<NodeProcess> nodeProcesses;
    private final ProcessDefinition definition;
    private final Process process;

    /**
     * Create instance of operation to set starting process readable flag.
     * 
     * @param subprocessesInstanstances
     *            Instances of subprocesses, which must be added to graph
     *            elements.
     * @param subject
     *            Current subject.
     */
    public StartedSubprocessesVisitor(User user, ProcessDefinition definition, Process process, List<NodeProcess> nodeProcesses) {
        this.user = user;
        this.definition = definition;
        this.process = process;
        this.nodeProcesses = nodeProcesses;
    }

    @Override
    public void onMultiSubprocess(MultiinstanceGraphElementPresentation element) {
        for (NodeProcess nodeProcess : nodeProcesses) {
            if (nodeProcess.getNodeId().equals(element.getNodeId())) {
                element.addSubprocessId(nodeProcess.getSubProcess().getId());
                if (checkPermission(nodeProcess.getSubProcess())) {
                    element.setReadPermission(true);
                }
            }
        }
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        if (element.isEmbedded()) {
            boolean b = ApplicationContextFactory.getProcessLogDAO().isNodeEntered(process, element.getNodeId());
            element.setReadPermission(b);
            element.setSubprocessId(process.getId());
            SubprocessDefinition subprocessDefinition = definition.getEmbeddedSubprocessesByName(element.getSubprocessName());
            element.setSubprocessName(subprocessDefinition.getNodeId());
        } else {
            for (NodeProcess nodeProcess : nodeProcesses) {
                if (Objects.equal(nodeProcess.getNodeId(), element.getNodeId())) {
                    element.setSubprocessId(nodeProcess.getSubProcess().getId());
                    if (checkPermission(nodeProcess.getSubProcess())) {
                        element.setReadPermission(true);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Check READ permission on process instance for current subject.
     * 
     * @param process
     *            Process instance to check READ permission.
     * @return true, if current actor can read process definition and false
     *         otherwise.
     */
    private boolean checkPermission(Process process) {
        PermissionDAO permissionDAO = ApplicationContextFactory.getPermissionDAO();
        return permissionDAO.isAllowed(user, ProcessPermission.READ, process);
    }

}
