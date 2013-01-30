package ru.runa.wfe.graph.image;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessesGraphElementAdapter;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.User;

/**
 * Operation to set starting process readable flag.
 */
public class SubprocessPermissionVisitor extends SubprocessesGraphElementAdapter {

    private static final Log log = LogFactory.getLog(SubprocessPermissionVisitor.class);

    /**
     * Current subject.
     */
    private final User user;

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
    public SubprocessPermissionVisitor(User user, IProcessDefinitionLoader loader) {
        this.user = user;
        this.loader = loader;
    }

    @Override
    public void onMultiSubprocess(MultiinstanceGraphElementPresentation element) {
        try {
            ProcessDefinition def = loader.getLatestDefinition(element.getSubprocessName());
            if (checkPermission(def)) {
                element.setReadPermission(true);
            }
            element.addSubprocessId(def.getId());
        } catch (DefinitionDoesNotExistException e) {
            log.warn("ProcessDefinitionDoesNotExistException", e);
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
    private boolean checkPermission(ProcessDefinition processDefinition) throws DefinitionDoesNotExistException, AuthenticationException {
        PermissionDAO permissionDAO = ApplicationContextFactory.getPermissionDAO();
        return permissionDAO.isAllowed(user, DefinitionPermission.READ, processDefinition);
    }
}
