package ru.runa.wfe.graph.image;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessesGraphElementAdapter;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;

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
    private final IProcessDefinitionLoader loader;

    /**
     * Create instance of operation to set subprocess definition readable flag.
     * 
     * @param subject
     *            Current subject.
     * @param jbpmContext
     *            {@link JbpmContext} to get jbpm data.
     * @param loader
     *            Process definition loader.
     */
    public SubprocessPermissionVisitor(Subject subject, IProcessDefinitionLoader loader) {
        this.subject = subject;
        this.loader = loader;
    }

    @Override
    public void onMultiinstance(MultiinstanceGraphElementPresentation element) {
        try {
            ProcessDefinition def = loader.getLatestDefinition(element.getSubprocessName());
            if (checkPermission(def)) {
                element.setReadPermission(true);
            }
            element.addSubprocessId(def.getId());
        } catch (DefinitionDoesNotExistException e) {
            log.warn("ProcessDefinitionDoesNotExistException", e);
        } catch (Exception e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        try {
            ProcessDefinition def = loader.getLatestDefinition(element.getSubprocessName());
            if (checkPermission(def)) {
                element.setReadPermission(true);
            }
            element.setSubprocessId(def.getId());
        } catch (DefinitionDoesNotExistException e) {
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
     * @return true, if current actor can read process definition and false otherwise.
     */
    private boolean checkPermission(ProcessDefinition processDefinition) throws DefinitionDoesNotExistException, AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        PermissionDAO permissionDAO = ApplicationContextFactory.getPermissionDAO();
        return permissionDAO.isAllowed(actor, DefinitionPermission.READ, processDefinition);
    }
}
