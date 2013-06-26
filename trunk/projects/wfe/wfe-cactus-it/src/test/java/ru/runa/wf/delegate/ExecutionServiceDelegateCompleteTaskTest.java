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
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class ExecutionServiceDelegateCompleteTaskTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private WfTask task;

    private Map<String, Object> legalVariables;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());
        // task =
        // executionDelegate.getTasks(helper.getAuthorizedPerformerUser(),
        // helper.getTaskBatchPresentation())[0];

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("amount.asked", (double) 200);
        legalVariables.put("amount.granted", (double) 150);
        legalVariables.put("approved", "true");

        super.setUp();
    }

    private void initTask() throws AuthorizationException, AuthenticationException {
        List<WfTask> tasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), helper.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        task = tasks.get(0);
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testCompleteTaskByAuthorizedSubject() throws Exception {
        initTask();

        assertEquals("state name differs from expected", "evaluating", task.getName());
        assertEquals("task <evaluating> is assigned before completeTask()", helper.getBossGroup(), task.getOwner());

        executionService.completeTask(helper.getAuthorizedPerformerUser(), task.getId(), legalVariables, null);
        List<WfTask> tasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), helper.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());
        assertEquals("task <treating collegues on cake and pie> is not assigned after starting [requester]", helper.getBossGroup(), task.getOwner());
        executionService.completeTask(helper.getAuthorizedPerformerUser(), tasks.get(0).getId(), legalVariables, null);

        tasks = executionService.getTasks(helper.getErpOperatorUser(), helper.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "updating erp asynchronously", tasks.get(0).getName());
        assertEquals("task <updating erp asynchronously> is not assigned before competeTask()", helper.getBossGroup(), task.getOwner());
    }

    public void testCompleteTaskBySubjectWhichIsNotInSwimlane() throws Exception {
        initTask();
        try {
            helper.removeExecutorFromGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());
            executionService.completeTask(helper.getAuthorizedPerformerUser(), task.getId(), legalVariables, null);
            fail("testCompleteTaskByNullSubject(), no Exception");
        } catch (AuthorizationException e) {
        }
    }

    public void testCompleteTaskByUnauthorizedSubject() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getUnauthorizedPerformerUser(), task.getId(), legalVariables, null);
            fail("testCompleteTaskByNullSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testCompleteTaskByNullSubject() throws Exception {
        initTask();
        try {
            executionService.completeTask(null, task.getId(), legalVariables, null);
            fail("testCompleteTaskByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCompleteTaskByFakeSubject() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getFakeUser(), task.getId(), legalVariables, null);
            fail("testCompleteTaskByFakeSubject(), no AuthenticationException");
        } catch (AuthorizationException e) {
            fail("testCompleteTaskByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testCompleteTaskByAuthorizedSubjectWithInvalidTaskId() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getAuthorizedPerformerUser(), -1l, legalVariables, null);
            fail("testCompleteTaskByAuthorizedSubjectWithInvalidTaskId(), no TaskDoesNotExistException");
        } catch (TaskDoesNotExistException e) {
        }
    }
}
