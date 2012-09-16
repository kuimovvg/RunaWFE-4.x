package ru.runa.wf.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.WeakPasswordException;
import ru.runa.delegate.impl.AdminScriptServiceDelegateRemoteImpl;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.WfeScriptException;
import ru.runa.wf.form.VariablesValidationException;

import com.google.common.collect.Lists;

public class WebWfScriptServiceTestHelper extends WfServiceTestHelper {

    private AdminScriptService wfeScriptServiceDelegate;

    public WebWfScriptServiceTestHelper(String testClassPrefixName) throws IOException, ExecutorOutOfDateException, ExecutorAlreadyExistsException,
            AuthorizationException, AuthenticationException, InternalApplicationException, UnapplicablePermissionException, WeakPasswordException {
        super(testClassPrefixName);
        createDelegate();
    }

    public void releaseResources() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        wfeScriptServiceDelegate = null;
        super.releaseResources();
    }

    private void createDelegate() {
        wfeScriptServiceDelegate = new AdminScriptServiceDelegateRemoteImpl();
    }

    public boolean isAllowedToExecutor(Identifiable identifiable, Executor executor, Permission permission) throws ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException, InternalApplicationException {
        Collection<Permission> permissions = authorizationService.getPermissions(adminSubject, executor, identifiable);
        return permissions.contains(permissions);
    }

    public boolean isAllowedToExecutorOnDefinition(Permission permission, Executor executor, String processDefinitionName)
            throws ProcessDefinitionDoesNotExistException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        ProcessDefinition definition = definitionService.getLatestProcessDefinitionStub(adminSubject, processDefinitionName);
        return isAllowedToExecutor(definition, executor, permission);
    }

    public boolean areExecutorsWeaklyEqual(Executor e1, Executor e2) {
        if (e1 == null || e2 == null) {
            return false;
        }
        if (!e1.getName().equals(e2.getName())) {
            return false;
        }
        if (!e1.getName().equals(e2.getName())) {
            return false;
        }
        if (!e1.getDescription().equals(e2.getDescription())) {
            return false;
        }
        if ((e1 instanceof Actor) && (e2 instanceof Actor)) {
            Actor a1 = (Actor) e1;
            Actor a2 = (Actor) e2;
            if (!a1.getFullName().equals(a2.getFullName())) {
                return false;
            }
        }
        return true;

    }

    public void executeScript(String resourceName) throws IOException, ExecutorOutOfDateException, WfeScriptException, AuthenticationException,
            AuthorizationException {
        wfeScriptServiceDelegate.run(getAdministrator().getName(), getAdministratorPassword(), readBytesFromFile(resourceName), new byte[0][]);
    }

    public ProcessInstanceStub startProcessInstance(String processDefinitionName, Executor performer) throws ProcessDefinitionDoesNotExistException,
            UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException, VariablesValidationException {
        List<Permission> validPermissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        getAuthorizationService().setPermissions(adminSubject, performer, validPermissions,
                getDefinitionService().getLatestProcessDefinitionStub(adminSubject, processDefinitionName));
        getExecutionService().startProcessInstance(adminSubject, processDefinitionName);
        return getExecutionService().getProcessInstanceStubs(adminSubject, getProcessInstanceBatchPresentation()).get(0);
    }

}
