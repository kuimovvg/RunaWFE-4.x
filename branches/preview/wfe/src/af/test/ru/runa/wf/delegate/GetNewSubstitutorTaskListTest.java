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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.ActorPermission;
import ru.runa.af.Permission;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.SubstitutionCriteriaSwimlane;
import ru.runa.af.SystemPermission;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

public class GetNewSubstitutorTaskListTest extends ServletTestCase {
    private final static String PREFIX = GetNewSubstitutorTaskListTest.class.getName();

    private static final String PROCESS_FILE_URL = WfServiceTestHelper.ONE_SWIMLANE_FILE_NAME;
    private final static String PROCESS_NAME = WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME;

    private final static String nameSubstitutedActor = "substitutedActor";
    private final static String nameSubstitutor = "substitutor";
    private final static String nameSubstitutor2 = "substitutor2";

    private final static String pwdSubstitutedActor = "substitutedActor";
    private final static String pwdSubstitutor = "substitutor";
    private final static String pwdSubstitutor2 = "substitutor2";

    private Actor substitutedActor;
    private Actor substitutor;
    private Actor substitutor2;

    private Subject substitutedActorSubject = null;
    private Subject substitutorSubject = null;
    private Subject substitutor2Subject = null;

    private SubstitutionCriteria substitutionCriteria_always;
    private SubstitutionCriteriaSwimlane substitutionCriteria_requester;
    private SubstitutionCriteriaSwimlane substitutionCriteria_no_requester;

    private WfServiceTestHelper testHelper;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(GetNewSubstitutorTaskListTest.class);
    }

    protected void setUp() throws Exception {
        testHelper = new WfServiceTestHelper(PREFIX);

        substitutedActor = testHelper.createActorIfNotExist(nameSubstitutedActor, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminSubject(), substitutedActor, nameSubstitutedActor);
        substitutor = testHelper.createActorIfNotExist(nameSubstitutor, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminSubject(), substitutor, nameSubstitutor);
        substitutor2 = testHelper.createActorIfNotExist(nameSubstitutor2, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminSubject(), substitutor2, nameSubstitutor2);

        {
            Collection<Permission> perm = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitutedActor, perm, ASystem.SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitutor, perm, ASystem.SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitutor2, perm, ASystem.SYSTEM);
        }
        {
            Collection<Permission> perm = Lists.newArrayList(ActorPermission.READ);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitutedActor, perm, substitutor);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitutor, perm, substitutedActor);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitutor2, perm, substitutedActor);
        }

        substitutedActorSubject = testHelper.getAuthenticationService().authenticate(nameSubstitutedActor, pwdSubstitutedActor);
        substitutorSubject = testHelper.getAuthenticationService().authenticate(nameSubstitutor, pwdSubstitutor);
        substitutor2Subject = testHelper.getAuthenticationService().authenticate(nameSubstitutor2, pwdSubstitutor2);

        substitutionCriteria_always = null;
        testHelper.createSubstitutionCriteria(substitutionCriteria_always);
        substitutionCriteria_requester = new SubstitutionCriteriaSwimlane();
        substitutionCriteria_requester.setConf(PROCESS_NAME + ".requester");
        substitutionCriteria_requester.setName(PROCESS_NAME + ".requester");
        testHelper.createSubstitutionCriteria(substitutionCriteria_requester);
        substitutionCriteria_no_requester = new SubstitutionCriteriaSwimlane();
        substitutionCriteria_no_requester.setConf(PROCESS_NAME + ".No_requester");
        substitutionCriteria_no_requester.setName(PROCESS_NAME + ".No_requester");
        testHelper.createSubstitutionCriteria(substitutionCriteria_no_requester);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_FILE_URL);
        testHelper.getDefinitionService().deployProcessDefinition(testHelper.getAdminSubject(), parBytes, Lists.newArrayList("testProcess"));
        ProcessDefinition definition = testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                PROCESS_NAME);
        Collection<Permission> definitionPermission = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS);
        testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitutedActor, definitionPermission, definition);

        batchPresentation = testHelper.getTaskBatchPresentation();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        testHelper.getDefinitionService().undeployProcessDefinition(testHelper.getAdminSubject(), PROCESS_NAME);
        testHelper.releaseResources();
        testHelper.removeSubstitutionCriteria(substitutionCriteria_always);
        testHelper.removeSubstitutionCriteria(substitutionCriteria_requester);
        testHelper.removeSubstitutionCriteria(substitutionCriteria_no_requester);
        super.tearDown();
    }

    /* Simple test case. Using process one_swimline_process and one substitutor with always subsitution
     * rules. Checking correct task's list on active/inactive actors.
     * */
    public void testSubstitutionSimple() throws Exception {
        Substitution substitution1 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will check precondition - no tasks to all actor's
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }

        testHelper.getExecutionService().startProcessInstance(substitutedActorSubject, PROCESS_NAME);

        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 1);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        testHelper.setActorStatus(substitutedActor.getId(), true);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        List<TaskStub> tasks;
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutedActorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 1);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionByCriteria() throws Exception {
        Substitution substitution1 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }

        testHelper.getExecutionService().startProcessInstance(substitutedActorSubject, PROCESS_NAME);

        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 1);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        testHelper.setActorStatus(substitutedActor.getId(), true);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        List<TaskStub> tasks;
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutedActorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 1);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionByFalseCriteria() throws Exception {
        Substitution substitution1 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_no_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }

        testHelper.getExecutionService().startProcessInstance(substitutedActorSubject, PROCESS_NAME);

        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        testHelper.setActorStatus(substitutedActor.getId(), true);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        List<TaskStub> tasks;
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutedActorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutedActorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionFalseTermination() throws Exception {
        Substitution substitution1 = testHelper.createTerminator(substitutedActorSubject, substitutionCriteria_no_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution3 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }

        testHelper.getExecutionService().startProcessInstance(substitutedActorSubject, PROCESS_NAME);

        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 1);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        testHelper.setActorStatus(substitutedActor.getId(), true);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        List<TaskStub> tasks;
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutedActorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 1);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 1);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
        testHelper.removeCriteriaFromSubstitution(substitution3);
    }

    public void testSubstitutionTrueTermination() throws Exception {
        Substitution substitution1 = testHelper.createTerminator(substitutedActorSubject, substitutionCriteria_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution3 = testHelper.createActorSubstitutor(substitutedActorSubject,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }

        testHelper.getExecutionService().startProcessInstance(substitutedActorSubject, PROCESS_NAME);

        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        testHelper.setActorStatus(substitutedActor.getId(), true);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        List<TaskStub> tasks;
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutedActorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutedActor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), false);
        {
            checkTaskList(substitutedActorSubject, 1);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.setActorStatus(substitutor.getId(), true);
        tasks = testHelper.getExecutionService().getTasks(substitutedActorSubject, batchPresentation);
        testHelper.getExecutionService().completeTask(substitutorSubject, tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(substitutedActorSubject, 0);
            checkTaskList(substitutorSubject, 0);
            checkTaskList(substitutor2Subject, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
        testHelper.removeCriteriaFromSubstitution(substitution3);
    }

    private void checkTaskList(Subject subject, int expectedLength) throws Exception {
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        // Let's change actor status to check correct working.
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        boolean actorStatus = actor.isActive();
        testHelper.getExecutorService().setStatus(testHelper.getAdminSubject(), actor.getId(), !actorStatus);
        testHelper.getExecutorService().setStatus(testHelper.getAdminSubject(), actor.getId(), actorStatus);
        tasks = testHelper.getExecutionService().getTasks(subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        actorStatus = testHelper.getExecutorService().getActor(testHelper.getAdminSubject(), substitutedActor.getId()).isActive();
        testHelper.getExecutorService().setStatus(testHelper.getAdminSubject(), substitutedActor.getId(), !actorStatus);
        if (!actorStatus) {
            tasks = testHelper.getExecutionService().getTasks(substitutorSubject, batchPresentation);
            assertEquals("getTasks() returns wrong tasks number (expected " + 0 + ", but was " + tasks.size() + ")", 0, tasks.size());
            tasks = testHelper.getExecutionService().getTasks(substitutor2Subject, batchPresentation);
            assertEquals("getTasks() returns wrong tasks number (expected " + 0 + ", but was " + tasks.size() + ")", 0, tasks.size());
        }
        testHelper.getExecutorService().setStatus(testHelper.getAdminSubject(), substitutedActor.getId(), actorStatus);
        tasks = testHelper.getExecutionService().getTasks(subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
    }
}
