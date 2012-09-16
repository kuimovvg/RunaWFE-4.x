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
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class GetTaskListTest extends ServletTestCase {
    private final static String PREFIX = GetTaskListTest.class.getName();

    private WfServiceTestHelper th;

    private byte[] parBytes;

    // see par file for explanation
    private final static String PROCESS_NAME = "simple process";

    private final static String ACTOR3_NAME = "actor3";

    private final static String ACTOR3_PASSWD = "actor3";

    private final static String GROUP1_NAME = "group1";

    private final static String GROUP2_NAME = "group2";

    private Group group1;

    private Group group2;

    private Actor actor3;

    private Subject actor3Subject;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(GetTaskListTest.class);
    }

    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(PREFIX);

        actor3 = th.createActorIfNotExist(ACTOR3_NAME, PREFIX);
        th.getExecutorService().setPassword(th.getAdminSubject(), actor3, ACTOR3_PASSWD);
        group1 = th.createGroupIfNotExist(GROUP1_NAME, PREFIX);
        group2 = th.createGroupIfNotExist(GROUP2_NAME, PREFIX);

        parBytes = WfServiceTestHelper.readBytesFromFile(WfServiceTestHelper.ORGANIZATION_FUNCTION_PAR_FILE_NAME);

        Collection<Permission> p = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
        th.getAuthorizationService().setPermissions(th.getAdminSubject(), actor3, p, ASystem.SYSTEM);
        actor3Subject = DelegateFactory.getInstance().getAuthenticationService().authenticate(actor3.getName(), ACTOR3_PASSWD);
        th.setPermissionsToAuthorizedPerformerOnSystem(th.getAuthorizationService().getAllPermissions(ASystem.SYSTEM));

        th.getDefinitionService().deployProcessDefinition(th.getAuthorizedPerformerSubject(), parBytes, Lists.newArrayList("testProcess"));
        batchPresentation = th.getTaskBatchPresentation();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        th.getDefinitionService().undeployProcessDefinition(th.getAuthorizedPerformerSubject(), PROCESS_NAME);
        parBytes = null;
        th.releaseResources();
        th = null;
        actor3 = null;
        group1 = null;
        group2 = null;
        actor3Subject = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testEqualsFunctionTest() throws Exception {

        th.getExecutionService().startProcessInstance(th.getAuthorizedPerformerSubject(), PROCESS_NAME);
        // assignment handler creates secured object for swimlane and grant group2 permissions on it, so we have to update reference
        group2 = (Group) th.getExecutor(group2.getName());

        List<TaskStub> tasks;

        tasks = th.getExecutionService().getTasks(actor3Subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = th.getExecutionService().getTasks(th.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), group2);
        tasks = th.getExecutionService().getTasks(th.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", th.getAuthorizedPerformerActor(), tasks.get(0).getTargetActor());

        th.getExecutionService().completeTask(th.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        tasks = th.getExecutionService().getTasks(th.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = th.getExecutionService().getTasks(actor3Subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", actor3, tasks.get(0).getTargetActor());

        th.getExecutionService().completeTask(actor3Subject, tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());
        // same as commented higher
        actor3 = (Actor) th.getExecutor(actor3.getName());
        group2 = (Group) th.getExecutor(group2.getName());

        tasks = th.getExecutionService().getTasks(actor3Subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        th.addExecutorToGroup(actor3, group1);
        tasks = th.getExecutionService().getTasks(actor3Subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = th.getExecutionService().getTasks(th.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        th.removeExecutorFromGroup(th.getAuthorizedPerformerActor(), group2);
        tasks = th.getExecutionService().getTasks(th.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", th.getAuthorizedPerformerActor(), tasks.get(0).getTargetActor());

        th.getExecutionService().completeTask(th.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(),
                tasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());
    }
}
