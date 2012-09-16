package ru.runa.wf.graph;

import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.TmpApplicationContextFactory;
import ru.runa.af.logic.CommonLogic;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;

/**
 * Operation to add identities of started subprocesses to graph elements.
 */
public class StartedSubprocessesVisitor extends SubprocessesGraphElementAdapter {

    private static final Log log = LogFactory.getLog(StartedSubprocessesVisitor.class);

    /**
     * Current subject.
     */
    private final Subject subject;

    /**
     * Instances of subprocesses, which must be added to graph elements.
     */
    private final List<StartedSubprocesses> subprocessesInstanstances;

    /**
     * Create instance of operation to set starting process readable flag.
     * 
     * @param subprocessesInstanstances
     *            Instances of subprocesses, which must be added to graph elements.
     * @param subject
     *            Current subject.
     */
    public StartedSubprocessesVisitor(Subject subject, List<StartedSubprocesses> subprocessesInstanstances) {
        this.subject = subject;
        this.subprocessesInstanstances = subprocessesInstanstances;
    }

    @Override
    public void onMultiinstance(MultiinstanceGraphElementPresentation element) {
        try {
            for (StartedSubprocesses subprocess : subprocessesInstanstances) {
                if (subprocess.getNode().getName().equals(element.getName())) {
                    element.addSubprocessId(subprocess.getSubProcessInstance().getId());
                    if (checkPermission(subprocess.getSubProcessInstance())) {
                        element.setReadPermission(true);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        try {
            for (StartedSubprocesses subprocess : subprocessesInstanstances) {
                if (subprocess.getNode().getName().equals(element.getName())) {
                    element.setSubprocessId(subprocess.getSubProcessInstance().getId());
                    if (checkPermission(subprocess.getSubProcessInstance())) {
                        element.setReadPermission(true);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

    /**
     * Check READ permission on process instance for current subject.
     * 
     * @param processInstance
     *            Process instance to check READ permission.
     * @return true, if current actor can read process definition and false otherwise.
     */
    private boolean checkPermission(ProcessInstance processInstance) throws ProcessDefinitionDoesNotExistException, AuthenticationException {
        ProcessInstanceStub stub = new ProcessInstanceStub(processInstance);
        CommonLogic commonLogic = TmpApplicationContextFactory.getCommonLogic();
        return commonLogic.isPermissionAllowed(subject, stub, ProcessInstancePermission.READ);
    }

}
