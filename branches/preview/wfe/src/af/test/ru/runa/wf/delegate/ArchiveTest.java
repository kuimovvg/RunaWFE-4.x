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
package ru.runa.wf.delegate;

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;

/**
 * Archiving functionality tests.
  * @author Konstantinov Aleksey 22.01.2012
  */
public class ArchiveTest extends ServletTestCase {

    /**
     * Set this flag to true, to perform full archiving testing. 
     * By default only base functionality is tested (archiving tests is too long to enable it in all case).
     */
    private final boolean abs__runComplexTests = false;

    // Test processes name
    private final String subProcess3 = "Archiving - Subprocess3";
    private final String subProcess2 = "Archiving - Subprocess2";
    private final String subProcess1 = "Archiving - Subprocess1";
    private final String mainProcess = "Archiving - Main";

    /**
     * Default main tests actor (which execute process) name.
     */
    private final String mainActorName = "main";

    /**
     * Default main tests actor (which execute process) code.
     */
    private final long mainActorCode = 11223344;

    /**
     * System administrator subject.
     */
    private Subject adminSubject;

    /**
     * Main test actor.
     */
    private Actor mainActor;

    /**
     * Main test actor subject.
     */
    private Subject mainSubject;

    public static Test suite() {
        return new TestSuite(ArchiveTest.class);
    }

//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        if (!abs__runComplexTests) {
//            return;
//        }
//    }
//
//    @Override
//    protected void tearDown() throws Exception {
//        if (abs__runComplexTests) {
//        }
//        super.tearDown();
//    }

    /**
     * Simple test: start one process instance and archive/delete it.
     */
    public void testArchiveAndDelete() throws Exception {
//        if (!abs__runComplexTests) {
//            return;
//        }
//        initTestEnvironment();
//        archiveAndDelete();
//        checkArtifactsRemoved();
//        clearTest();
    }

//    /**
//     * Simple test: start one process instance and archive it twice. 
//     * After archiving process instance removed from main database.
//     * Logs is moved to GBPM_LOG_N before archiving.
//     */
//    public void testArchiveTwice() throws Exception {
//        if (!abs__runComplexTests) {
//            return;
//        }
//        initTestEnvironment();
//        ExecutionServiceDelegate executionDelegate = ru.runa.wf.delegate.DelegateFactory.getInstance().getExecutionServiceDelegate();
//        startProcessInstances(mainActor, mainSubject, executionDelegate, 1);
//        BatchPresentation processInstanceBatch = WFProfileStrategy.PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
//        ProcessInstanceStub[] initialInstances = executionDelegate.getProcessInstanceStubs(adminSubject, processInstanceBatch);
//        for (int i = 0; i < 2; ++i) {
//            for (ProcessInstanceStub processInstance : initialInstances) {
//                executionDelegate.archiveProcessInstances(adminSubject, null, null, null, 0, processInstance.getId(), 0, true, false);
//            }
//        }
//        HashMap<Integer, FilterCriteria> filters = new HashMap<Integer, FilterCriteria>();
//        filters.put(1, new StringFilterCriteria(new String[] { mainProcess }));
//        processInstanceBatch.setFilteredFieldsMap(filters);
//        for (ProcessInstanceStub processInstance : executionDelegate.getProcessInstanceStubs(adminSubject, processInstanceBatch)) {
//            executionDelegate.removeProcessInstances(adminSubject, null, null, null, 0, processInstance.getId(), 0, true, false);
//        }
//        for (ProcessInstanceStub processInstance : initialInstances) {
//            checkProcessInstanceLogsRemoved(processInstance.getId());
//        }
//        checkArtifactsRemoved();
//        clearTest();
//    }
//
//    /**
//     * Test archiving if executor is removed and added again (with same code, name and executor group relations).
//     */
//    public void testArchiveAndDeleteReplacedActor() throws Exception {
//        if (!abs__runComplexTests) {
//            return;
//        }
//        initTestEnvironment();
//        archiveAndDelete(true);
//        AuthorizationServiceDelegate authorizationServiceDelegate = DelegateFactory.getInstance().getAuthorizationServiceDelegate();
//        DefinitionServiceDelegate definitionDelegate = ru.runa.wf.delegate.DelegateFactory.getInstance().getDefinitionServiceDelegate();
//        ExecutorServiceDelegate executorDelegate = DelegateFactory.getInstance().getExecutorServiceDelegate();
//        executorDelegate.remove(adminSubject, new long[] { mainActor.getId() });
//        mainActor = executorDelegate.create(adminSubject, new Actor(mainActorName, "", "", mainActorCode));
//        executorDelegate.setPassword(adminSubject, mainActor, "123");
//        mainSubject = DelegateFactory.getInstance().getAuthenticationServiceDelegate().authenticate(mainActorName, "123");
//        Group multiGroup = executorDelegate.getGroup(adminSubject, "MultiGroup");
//        executorDelegate.addExecutorsToGroup(adminSubject, new Executor[] { mainActor, }, multiGroup);
//        grantProcessDefinitionPermissions(authorizationServiceDelegate, definitionDelegate);
//        archiveAndDelete(true);
//        checkArtifactsRemoved();
//        clearTest();
//    }
//
//    /**
//     * Test archiving if executor is removed and added new executor with same name but different code.
//     */
//    public void testArchiveAndDeleteReplacedNewActor() throws Exception {
//        if (!abs__runComplexTests) {
//            return;
//        }
//        initTestEnvironment();
//        archiveAndDelete(true);
//        AuthorizationServiceDelegate authorizationServiceDelegate = DelegateFactory.getInstance().getAuthorizationServiceDelegate();
//        DefinitionServiceDelegate definitionDelegate = ru.runa.wf.delegate.DelegateFactory.getInstance().getDefinitionServiceDelegate();
//        ExecutorServiceDelegate executorDelegate = DelegateFactory.getInstance().getExecutorServiceDelegate();
//        executorDelegate.remove(adminSubject, new long[] { mainActor.getId() });
//        mainActor = executorDelegate.create(adminSubject, new Actor(mainActorName, "", "", mainActorCode * 2));
//        executorDelegate.setPassword(adminSubject, mainActor, "123");
//        mainSubject = DelegateFactory.getInstance().getAuthenticationServiceDelegate().authenticate(mainActorName, "123");
//        Group multiGroup = executorDelegate.getGroup(adminSubject, "MultiGroup");
//        executorDelegate.addExecutorsToGroup(adminSubject, new Executor[] { mainActor, }, multiGroup);
//        grantProcessDefinitionPermissions(authorizationServiceDelegate, definitionDelegate);
//        archiveAndDelete(true);
//        checkArtifactsRemoved();
//        clearTest();
//    }
//
//    /**
//     * Test archiving if executor is removed from group and added back.
//     */
//    public void testArchiveAndDeleteGroupRelationChange() throws Exception {
//        if (!abs__runComplexTests) {
//            return;
//        }
//        initTestEnvironment();
//        archiveAndDelete(true);
//        ExecutorServiceDelegate executorDelegate = DelegateFactory.getInstance().getExecutorServiceDelegate();
//        Group multiGroup = executorDelegate.getGroup(adminSubject, "MultiGroup");
//        executorDelegate.removeExecutorsFromGroup(adminSubject, new Executor[] { mainActor, }, multiGroup);
//        executorDelegate.addExecutorsToGroup(adminSubject, new Executor[] { mainActor, }, multiGroup);
//        archiveAndDelete(true);
//        checkArtifactsRemoved();
//        clearTest();
//    }
//
//    /**
//     * Initialize test environment.
//     */
//    private void initTestEnvironment() throws Exception {
//        new InitializerServiceDelegateRemoteImpl().init(true, false);
//        new InitializerServiceDelegateRemoteImpl().init(true, true);
//        ru.runa.wf.delegate.DelegateFactory wfDelegateFactory = ru.runa.wf.delegate.DelegateFactory.getInstance();
//        AuthorizationServiceDelegate authorizationServiceDelegate = DelegateFactory.getInstance().getAuthorizationServiceDelegate();
//        DefinitionServiceDelegate definitionDelegate = wfDelegateFactory.getDefinitionServiceDelegate();
//        adminSubject = DelegateFactory.getInstance().getAuthenticationServiceDelegate().authenticate("Administrator", "wf");
//        mainActor = createActors(adminSubject);
//        mainSubject = DelegateFactory.getInstance().getAuthenticationServiceDelegate().authenticate(mainActorName, "123");
//        deployProcessDefinitions(authorizationServiceDelegate, definitionDelegate);
//    }
//
//    /**
//     * Clears artifacts after test.
//     */
//    private void clearTest() throws Exception {
//        new InitializerServiceDelegateRemoteImpl().init(true, false);
//        new InitializerServiceDelegateRemoteImpl().init(true, true);
//    }
//
//    /**
//     * Searches for artifacts (objects, logs) in main database, which must be removed, but still present.  
//     */
//    private void checkArtifactsRemoved() {
//        Session session = HibernateSessionFactory.openSession();
//        try {
//            checkCount(session, ProcessInstance.class);
//            checkCount(session, ProcessLog.class);
//            checkCount(session, VariableInstance.class);
//            checkCount(session, StartedSubprocesses.class);
//            checkCount(session, PassedTransition.class);
//            checkCount(session, SwimlaneInstance.class);
//            checkCount(session, TaskInstance.class);
//            checkCount(session, Token.class);
//            checkCount(session, TokenVariableMap.class);
//            checkCount(session, SwimlaneInstance.class);
//            checkCount(session, SwimlaneInstance.class);
//        } finally {
//            HibernateSessionFactory.closeSession(true);
//        }
//    }
//
//    /**
//     * Loads objects count stored in main database. Throws exception, if at least one object found.
//     * @param session Hibernate session to load data from main database.
//     * @param clazz Counting object type.
//     */
//    private void checkCount(Session session, Class clazz) {
//        long count = ((Number) (session.createQuery("select count(instance) from " + clazz.getName() + " as instance").uniqueResult())).longValue();
//        assertEquals("Found " + clazz.getName(), 0, count);
//    }
//
//    /**
//     * Start and completes one process instance and archive it. Check process archived correct and correct removed after what.
//     * @param logsMoved Flag, equals true, if logs must be moved to JBPM_LOG_N table before archiving.
//     */
//    private void archiveAndDelete() throws Exception {
//        ExecutionServiceDelegate executionDelegate = ru.runa.wf.delegate.DelegateFactory.getInstance().getExecutionServiceDelegate();
//        startProcessInstances(mainActor, mainSubject, executionDelegate, 1);
//        BatchPresentation processInstanceBatch = WFProfileStrategy.PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
//        processInstanceBatch.setRangeSize(500);
//        ProcessInstanceStub[] initialInstances = executionDelegate.getProcessInstanceStubs(adminSubject, processInstanceBatch);
//        for (ProcessInstanceStub processInstance : initialInstances) {
//            executionDelegate.archiveProcessInstances(adminSubject, null, null, null, 0, processInstance.getId(), 0, true, false);
//        }
//        HashMap<Integer, FilterCriteria> filters = new HashMap<Integer, FilterCriteria>();
//        filters.put(1, new StringFilterCriteria(new String[] { mainProcess }));
//        processInstanceBatch.setFilteredFieldsMap(filters);
//        for (ProcessInstanceStub processInstance : executionDelegate.getProcessInstanceStubs(adminSubject, processInstanceBatch)) {
//            executionDelegate.removeProcessInstances(adminSubject, null, null, null, 0, processInstance.getId(), 0, true, false);
//        }
//        for (ProcessInstanceStub processInstance : initialInstances) {
//            checkProcessInstanceLogsRemoved(processInstance.getId());
//        }
//    }
//
//    /**
//     * Check if process instance logs is removed from main database.
//     * @param instanceId Process instance id.
//     */
//    private void checkProcessInstanceLogsRemoved(long instanceId) {
//        Session session = HibernateSessionFactory.openSession();
//        try {
//            Query query = session.createQuery("select count(pl) from " + ProcessLog.class.getName() + " as pl where pl.token.processInstance.id="
//                    + instanceId);
//            Object count = query.uniqueResult();
//            assertTrue(count == null || ((Number) count).longValue() == 0);
//            try {
//                StringBuilder queryString = new StringBuilder();
//                String tblName = "JBPM_LOG_" + ((instanceId / 1000) + 1);
//                queryString.append("select count(1) from ").append(tblName).append(" left join JBPM_TOKEN on ").append(tblName);
//                queryString.append(".TOKEN_=JBPM_TOKEN.ID_ left join JBPM_PROCESSINSTANCE on JBPM_TOKEN.PROCESSINSTANCE_=JBPM_PROCESSINSTANCE.ID_ ");
//                queryString.append("where JBPM_PROCESSINSTANCE.ID_=").append(instanceId);
//                SQLQuery sqlQuery = session.createSQLQuery(queryString.toString());
//                count = sqlQuery.uniqueResult();
//                assertTrue(count == null || ((Number) count).longValue() == 0);
//            } catch (Exception e) {
//            }
//        } finally {
//            HibernateSessionFactory.closeSession(true);
//        }
//    }
//
//    /**
//     * Start test process instances.
//     * @param mainActor Main actor for archiving test.
//     * @param mainSubject Main actor for archiving test subject.
//     * @param executionDelegate
//     * @param count Started process count.
//     */
//    private void startProcessInstances(Actor mainActor, Subject mainSubject, ExecutionServiceDelegate executionDelegate, int count) throws Exception {
//        for (int i = 0; i < count; ++i) {
//            executionDelegate.startProcessInstance(mainSubject, mainProcess);
//            BatchPresentation taskPresentation = WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
//            TaskStub[] tasks = executionDelegate.getTasks(mainSubject, taskPresentation);
//            while (tasks != null && tasks.length > 0) {
//                TaskStub task = tasks[0];
//                executionDelegate.completeTask(mainSubject, task.getId(), task.getName(), mainActor.getId(), new HashMap<String, Object>());
//                tasks = executionDelegate.getTasks(mainSubject, taskPresentation);
//            }
//        }
//    }
//
//    /**
//     * Deploys process definition for testing.
//     * @param authorizationServiceDelegate 
//     * @param definitionDelegate
//     * @param adminSubject Administrator subject.
//     * @param mainActor Main actor for archiving test.
//     */
//    private void deployProcessDefinitions(AuthorizationServiceDelegate authorizationServiceDelegate, DefinitionServiceDelegate definitionDelegate)
//            throws Exception {
//        deployProcessDefinition(definitionDelegate, adminSubject, subProcess3 + ".par");
//        deployProcessDefinition(definitionDelegate, adminSubject, subProcess2 + ".par");
//        deployProcessDefinition(definitionDelegate, adminSubject, subProcess1 + ".par");
//        deployProcessDefinition(definitionDelegate, adminSubject, mainProcess + ".par");
//        grantProcessDefinitionPermissions(authorizationServiceDelegate, definitionDelegate);
//    }
//
//    /**
//     * @param authorizationServiceDelegate
//     * @param definitionDelegate
//     */
//    private void grantProcessDefinitionPermissions(AuthorizationServiceDelegate authorizationServiceDelegate,
//            DefinitionServiceDelegate definitionDelegate) throws Exception {
//        Permission[] permissions = new Permission[] { ProcessDefinitionPermission.READ, ProcessDefinitionPermission.READ_STARTED_INSTANCE,
//                ProcessDefinitionPermission.START_PROCESS };
//        ProcessDefinitionDescriptor definition = definitionDelegate.getLatestProcessDefinitionStub(adminSubject, subProcess3);
//        authorizationServiceDelegate.setPermissions(adminSubject, mainActor, permissions, definition);
//        definition = definitionDelegate.getLatestProcessDefinitionStub(adminSubject, subProcess2);
//        authorizationServiceDelegate.setPermissions(adminSubject, mainActor, permissions, definition);
//        definition = definitionDelegate.getLatestProcessDefinitionStub(adminSubject, subProcess1);
//        authorizationServiceDelegate.setPermissions(adminSubject, mainActor, permissions, definition);
//        definition = definitionDelegate.getLatestProcessDefinitionStub(adminSubject, mainProcess);
//        authorizationServiceDelegate.setPermissions(adminSubject, mainActor, permissions, definition);
//    }
//
//    /**
//     * Creates actors, used in test.
//     * @param adminSubject Administrator subject.
//     * @param authorizationServiceDelegate 
//     * @return
//     */
//    private Actor createActors(Subject adminSubject) throws Exception {
//        ExecutorServiceDelegate executorServiceDelegate = DelegateFactory.getInstance().getExecutorServiceDelegate();
//        AuthorizationServiceDelegate authorizationServiceDelegate = DelegateFactory.getInstance().getAuthorizationServiceDelegate();
//        Actor main = executorServiceDelegate.create(adminSubject, new Actor(mainActorName, "Archiving main actor", "", mainActorCode));
//        executorServiceDelegate.setPassword(adminSubject, main, "123");
//        Group multi = executorServiceDelegate.create(adminSubject, new Group("MultiGroup", ""));
//        Actor sub1 = executorServiceDelegate.create(adminSubject, new Actor("sub1", "Archiving actor in MultiGroup"));
//        Actor sub2 = executorServiceDelegate.create(adminSubject, new Actor("sub2", "Archiving actor in MultiGroup"));
//        executorServiceDelegate.addExecutorsToGroup(adminSubject, new Executor[] { main, sub1, sub2 }, multi);
//        authorizationServiceDelegate.setPermissions(adminSubject, main, new Permission[] { ExecutorPermission.READ }, multi);
//        authorizationServiceDelegate.setPermissions(adminSubject, main, new Permission[] { ExecutorPermission.READ }, sub1);
//        authorizationServiceDelegate.setPermissions(adminSubject, main, new Permission[] { ExecutorPermission.READ }, sub2);
//        return main;
//    }
//
//    /**
//     * Deploys process definition.
//     * @param definitionDelegate Definition delegate to deploy process definition.
//     * @param adminSubject Administrator subject.
//     * @param definitionName Deploying definition name.
//     */
//    private void deployProcessDefinition(DefinitionServiceDelegate definitionDelegate, Subject adminSubject, String definitionName) throws Exception {
//        byte[] definitionData = WfServiceTestHelper.readBytesFromFile(definitionName);
//        definitionDelegate.deployProcessDefinition(adminSubject, definitionData, new String[] { "archiving tests" });
//    }
}
