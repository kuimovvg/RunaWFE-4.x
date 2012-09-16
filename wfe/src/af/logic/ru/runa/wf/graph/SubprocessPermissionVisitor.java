package ru.runa.wf.graph;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.TmpApplicationContextFactory;
import ru.runa.af.logic.CommonLogic;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.caches.ProcessDefCacheCtrl;

/**
 * Operation to set starting process readable flag.
 */
public class SubprocessPermissionVisitor extends SubprocessesGraphElementAdapter {

    private static final Log log = LogFactory.getLog(SubprocessPermissionVisitor.class);

    /**
     * Current subject.
     */
    private final Subject subject;

    /**
     * Process definition cache.
     */
    private final ProcessDefCacheCtrl processDefCache;

    /**
     * Create instance of operation to set subprocess definition readable flag.
     * 
     * @param subject
     *            Current subject.
     * @param jbpmContext
     *            {@link JbpmContext} to get jbpm data.
     * @param processDefCache
     *            Process definition cache.
     */
    public SubprocessPermissionVisitor(Subject subject, ProcessDefCacheCtrl processDefCache) {
        this.subject = subject;
        this.processDefCache = processDefCache;
    }

    @Override
    public void onMultiinstance(MultiinstanceGraphElementPresentation element) {
        try {
            ExecutableProcessDefinition def = processDefCache.getLatestDefinition(element.getSubprocessName());
            if (checkPermission(def)) {
                element.setReadPermission(true);
            }
            element.addSubprocessId(def.getId());
        } catch (ProcessDefinitionDoesNotExistException e) {
            log.warn("ProcessDefinitionDoesNotExistException", e);
        } catch (Exception e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        try {
            ExecutableProcessDefinition def = processDefCache.getLatestDefinition(element.getSubprocessName());
            if (checkPermission(def)) {
                element.setReadPermission(true);
            }
            element.setSubprocessId(def.getId());
        } catch (ProcessDefinitionDoesNotExistException e) {
            log.warn("ProcessDefinitionDoesNotExistException", e);
        } catch (AuthenticationException e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

    /**
     * Check READ permission on definition for current subject.
     * 
     * @param processDefinition
     *            Process definition to check READ permission.
     * @return true, if current actor can read process definition and false
     *         otherwise.
     */
    private boolean checkPermission(ExecutableProcessDefinition processDefinition) throws ProcessDefinitionDoesNotExistException,
            AuthenticationException {
        ProcessDefinition defStub = new ProcessDefinition(processDefinition);
        CommonLogic commonLogic = TmpApplicationContextFactory.getCommonLogic();
        return commonLogic.isPermissionAllowed(subject, defStub, ProcessDefinitionPermission.READ);
    }
}
