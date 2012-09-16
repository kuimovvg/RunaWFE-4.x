/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.WebServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionAlreadyExistsException;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.WorkflowSystemPermission;
import ru.runa.wf.presentation.WFProfileStrategy;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * <p>
 * Created on 05.11.2004
 * </p>
 * 
 * @author miheev_a  
 * @author semochkin_v@runa.ru
 * @author Gritsenko_S
 */
public class WebWfServiceTestHelper extends WebServiceTestHelper {

    private static final String PROCESS_DEFINITION_ADMINISTRATORS = "Process Definition Administrators";

    public final static String VALID_FILE_NAME = "validProcess.par";

    public final static String ONE_SWIMLANE_FILE_NAME = "oneSwimlaneProcess.par";

    public final static String INVALID_FILE_NAME = "invalidProcess.par";

    public final static String NONEXISTINGSWIMLINES_FILE_NAME = "NonExistingSwimlanes.par";

    public final static String ORGANIZATION_FUNCTION_PAR_FILE_NAME = "organizationfunction.par";

    public final static String VALID_PROCESS_NAME = "validProcess";

    public final static String ONE_SWIMLANE_PROCESS_NAME = "oneSwimlaneProcess";

    public final static String INVALID_PROCESS_NAME = "invalidProcess";

    public final static String SWIMLANE_PROCESS_NAME = "swimlaneProcess";

    public final static String SWIMLANE_PROCESS_FILE_NAME = "swimlaneProcess.par";

    public final static String DECISION_JPDL_PROCESS_NAME = "jpdlDecisionTestProcess";

    public final static String DECISION_JPDL_PROCESS_FILE_NAME = "jpdlDecisionTestProcess.par";

    public final static String FORK_JPDL_1_PROCESS_NAME = "jpdlFork1TestProcess";

    public final static String FORK_JPDL_1_PROCESS_FILE_NAME = "jpdlFork1TestProcess.par";

    public final static String FORK_JPDL_2_PROCESS_NAME = "jpdlFork2TestProcess";

    public final static String FORK_JPDL_2_PROCESS_FILE_NAME = "jpdlFork2TestProcess.par";

    public final static String FORK_FAULT_JPDL_PROCESS_NAME = "jpdlForkFaultTestProcess";

    public final static String FORK_FAULT_JPDL_PROCESS_FILE_NAME = "jpdlForkFaultTestProcess.par";

    public final static String TIMER_PROCESS_NAME = "timerProcess";

    public final static String SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME = "sameGroupRoleStateSequence";

    protected DefinitionService definitionService;

    protected ExecutionService executionService;

    private byte[] validDefinition;

    private byte[] invalidDefinition;

    private byte[] nonExistingSwimlanesDefinition;

    private final static String HR_ACTOR_NAME = "HrOperator";

    private final static String HR_ACTOR_PWD = "HrOperator";

    private final static String SWIMLANE2_ACTOR_NAME = "ErpOperator";

    private final static String SWIMLANE2_ACTOR_PWD = "ErpOperator";

    private final static String SWIMLANE1_GROUP_NAME = "BossGroup";

    private Actor erpOperator = null;

    private Subject erpOperatorSubject = null;

    private Actor hrOperator = null;

    private Subject hrOperatorSubject = null;

    private Group bossGroup = null;

    public WebWfServiceTestHelper(String testClassPrefixName) throws IOException, ExecutorOutOfDateException, ExecutorAlreadyExistsException,
            AuthorizationException, AuthenticationException, InternalApplicationException, UnapplicablePermissionException, WeakPasswordException {
        super(testClassPrefixName);

        try {
            Class exec = Class.forName("ru.runa.af.organizationfunction.ExecutorByNameFunction");
            exec.getDeclaredMethod("clearCache").invoke(exec, new Object[0]);
        } catch (ClassNotFoundException e) {
            throw new InternalApplicationException("ClassNotFoundExceptio");
        } catch (IllegalAccessException e) {
            throw new InternalApplicationException("IllegalAccessException");
        } catch (InvocationTargetException e) {
            throw new InternalApplicationException("IllegalAccessException");
        } catch (NoSuchMethodException e) {
            throw new InternalApplicationException("NoSuchMethodException");
        }

        createDelegates();
        createSampleDefinitions();
        createSwimlaneExecutors();
    }

    private void createSwimlaneExecutors() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, ExecutorAlreadyExistsException, UnapplicablePermissionException, WeakPasswordException {
        erpOperator = createActor(SWIMLANE2_ACTOR_NAME, "Actor in swimlane of test process");
        getExecutorService().setPassword(getAdminSubject(), erpOperator, SWIMLANE2_ACTOR_PWD);
        hrOperator = createActor(HR_ACTOR_NAME, "Actor in HR");
        getExecutorService().setPassword(getAdminSubject(), hrOperator, HR_ACTOR_PWD);
        bossGroup = createGroup(SWIMLANE1_GROUP_NAME, "Group in swimlane of test process");
        List<Permission> p = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
        getAuthorizationService().setPermissions(getAdminSubject(), erpOperator, p, ASystem.SYSTEM);
        erpOperatorSubject = DelegateFactory.getInstance().getAuthenticationService()
                .authenticate(erpOperator.getName(), SWIMLANE2_ACTOR_PWD);
        hrOperatorSubject = DelegateFactory.getInstance().getAuthenticationService().authenticate(hrOperator.getName(), HR_ACTOR_PWD);
    }

    /**
     * @return Returns the hrOperator.
     * @throws ExecutorOutOfDateException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Actor getHrOperator() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return getExecutorService().getActor(getAdminSubject(), hrOperator.getId());
    }

    /**
     * @return Returns the hrOperatorSubject.
     */
    public Subject getHrOperatorSubject() {
        return hrOperatorSubject;
    }

    /**
     * @return Returns the bossGroup.
     * @throws ExecutorOutOfDateException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Group getBossGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return getExecutorService().getGroup(getAdminSubject(), bossGroup.getId());
    }

    /**
     * @return Returns the erpOperator.
     * @throws ExecutorOutOfDateException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Actor getErpOperator() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return getExecutorService().getActor(getAdminSubject(), erpOperator.getId());
    }

    /**
     * @return Returns the erpOperatorSubject.
     */
    public Subject getErpOperatorSubject() {
        return erpOperatorSubject;
    }

    private void createSampleDefinitions() throws IOException {
        validDefinition = readBytesFromFile(VALID_FILE_NAME);
        invalidDefinition = readBytesFromFile(INVALID_FILE_NAME);
        nonExistingSwimlanesDefinition = readBytesFromFile(NONEXISTINGSWIMLINES_FILE_NAME);
    }

    @Override
    public void releaseResources() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        definitionService = null;

        executionService = null;
        erpOperator = null;
        erpOperatorSubject = null;
        bossGroup = null;
        super.releaseResources();
    }

    public void setPermissionsToAuthorizedPerformerOnDefinition(Collection<Permission> permissions, ProcessDefinition definition)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        authorizationService.setPermissions(adminSubject, getAuthorizedPerformerActor(), permissions, definition);
    }

    public void setPermissionsToAuthorizedPerformerOnProcessInstance(Collection<Permission> permissions, ProcessInstanceStub instance)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        authorizationService.setPermissions(adminSubject, getAuthorizedPerformerActor(), permissions, instance);
    }

    public void setPermissionsToAuthorizedPerformerOnDefinitionByName(Collection<Permission> permissions, String processDefinitionName)
            throws ProcessDefinitionDoesNotExistException, UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException {
        ProcessDefinition definition = definitionService.getLatestProcessDefinitionStub(adminSubject, processDefinitionName);
        authorizationService.setPermissions(adminSubject, getAuthorizedPerformerActor(), permissions, definition);
    }

    public byte[] getValidProcessDefinition() {
        return validDefinition;
    }

    public byte[] getInValidProcessDefinition() {
        return invalidDefinition;
    }

    public byte[] getProcessDefinitionWithNonExistingSwimlanes() {
        return nonExistingSwimlanesDefinition;
    }

    public void deployValidProcessDefinition() throws UnapplicablePermissionException, ExecutorOutOfDateException, InternalApplicationException,
            AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException, ProcessDefinitionArchiveException {
        List<Permission> deployPermissions = Lists.newArrayList(WorkflowSystemPermission.DEPLOY_DEFINITION);
        setPermissionsToAuthorizedPerformerOnSystem(deployPermissions);
        definitionService.deployProcessDefinition(getAuthorizedPerformerSubject(), getValidProcessDefinition(),
                Lists.newArrayList("testProcess"));
    }

    public void undeployValidProcessDefinition() throws InternalApplicationException, ProcessDefinitionDoesNotExistException,
            UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            SuperProcessInstanceExistsException {
        List<Permission> undeployPermissions = Lists.newArrayList(ProcessDefinitionPermission.UNDEPLOY_DEFINITION);
        setPermissionsToAuthorizedPerformerOnDefinitionByName(undeployPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        definitionService.undeployProcessDefinition(getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void deployValidProcessDefinition(String parResourceName) throws UnapplicablePermissionException, ExecutorOutOfDateException,
            InternalApplicationException, AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException,
            ProcessDefinitionArchiveException, IOException {
        List<Permission> deployPermissions = Lists.newArrayList(WorkflowSystemPermission.DEPLOY_DEFINITION);
        setPermissionsToAuthorizedPerformerOnSystem(deployPermissions);
        definitionService.deployProcessDefinition(getAuthorizedPerformerSubject(), readBytesFromFile(parResourceName),
                Lists.newArrayList("testProcess"));
    }

    public void undeployValidProcessDefinition(String parDefinitionName) throws InternalApplicationException, ProcessDefinitionDoesNotExistException,
            UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            SuperProcessInstanceExistsException {
        List<Permission> undeployPermissions = Lists.newArrayList(ProcessDefinitionPermission.UNDEPLOY_DEFINITION);
        setPermissionsToAuthorizedPerformerOnDefinitionByName(undeployPermissions, parDefinitionName);
        definitionService.undeployProcessDefinition(getAuthorizedPerformerSubject(), parDefinitionName);
    }

    private void createDelegates() {
        definitionService = DelegateFactory.getInstance().getDefinitionService();
        executionService = DelegateFactory.getInstance().getExecutionService();
    }

    public DefinitionService getDefinitionService() {
        return definitionService;
    }

    public ExecutionService getExecutionService() {
        return executionService;
    }

    public Group getProcessDefinitionAdministratorsGroup() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        return (Group) getExecutor(PROCESS_DEFINITION_ADMINISTRATORS);
    }

    public BatchPresentation getProcessDefinitionBatchPresentation() {
        return WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
    }

    public BatchPresentation getProcessDefinitionBatchPresentation(String presentationId) {
        return WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, presentationId);
    }

    public BatchPresentation getProcessInstanceBatchPresentation() {
        return WFProfileStrategy.PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
    }

    public BatchPresentation getProcessInstanceBatchPresentation(String presentationId) {
        return WFProfileStrategy.PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, presentationId);
    }

    public BatchPresentation getTaskBatchPresentation() {
        return WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
    }

    public BatchPresentation getTaskBatchPresentation(String presentationId) {
        return WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, presentationId);
    }

    public static byte[] readBytesFromFile(String fileName) throws IOException {
        InputStream is = WfServiceTestHelper.class.getResourceAsStream(fileName);
        return ByteStreams.toByteArray(is);
    }

    //TODO make all required tests use this method
    public static Map<String, Object> createVariablesMap(String variableName, Object variableValue) {
        Map<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put(variableName, variableValue);
        return variablesMap;
    }
}
