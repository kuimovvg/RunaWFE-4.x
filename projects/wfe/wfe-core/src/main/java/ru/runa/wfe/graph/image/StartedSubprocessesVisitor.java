package ru.runa.wfe.graph.image;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessesGraphElementAdapter;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Objects;

/**
 * Operation to add identities of started subprocesses to graph elements.
 */
public class StartedSubprocessesVisitor extends SubprocessesGraphElementAdapter {

    /**
     * Current subject.
     */
    private final Subject subject;

    /**
     * Instances of subprocesses, which must be added to graph elements.
     */
    private final List<NodeProcess> nodeProcesses;

    /**
     * Create instance of operation to set starting process readable flag.
     * 
     * @param subprocessesInstanstances
     *            Instances of subprocesses, which must be added to graph
     *            elements.
     * @param subject
     *            Current subject.
     */
    public StartedSubprocessesVisitor(Subject subject, List<NodeProcess> nodeProcesses) {
        this.subject = subject;
        this.nodeProcesses = nodeProcesses;
    }

    @Override
    public void onMultiSubprocess(MultiinstanceGraphElementPresentation element) {
        for (NodeProcess subprocess : nodeProcesses) {
            if (subprocess.getNodeId().equals(element.getNodeId())) {
                element.addSubprocessId(subprocess.getSubProcess().getId());
                if (checkPermission(subprocess.getSubProcess())) {
                    element.setReadPermission(true);
                }
            }
        }
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        for (NodeProcess subprocess : nodeProcesses) {
            if (Objects.equal(subprocess.getNodeId(), element.getNodeId())) {
                element.setSubprocessId(subprocess.getSubProcess().getId());
                if (checkPermission(subprocess.getSubProcess())) {
                    element.setReadPermission(true);
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
    private boolean checkPermission(Process process) throws DefinitionDoesNotExistException, AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        PermissionDAO permissionDAO = ApplicationContextFactory.getPermissionDAO();
        return permissionDAO.isAllowed(actor, ProcessPermission.READ, process);
    }

}
