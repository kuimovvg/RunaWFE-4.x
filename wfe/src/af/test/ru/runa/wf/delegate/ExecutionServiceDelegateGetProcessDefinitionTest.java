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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetProcessDefinitionTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private long taskId;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetProcessDefinitionTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);

        taskId = executionService.getTasks(helper.getAdminSubject(), helper.getTaskBatchPresentation()).get(0).getId();

        super.setUp();
    }

    private void initTaskId() throws AuthorizationException, AuthenticationException {
        List<TaskStub> tasks = executionService.getTasks(helper.getAdminSubject(), helper.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        taskId = tasks.get(0).getId();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetProcessDefinitionByAuthorizedSubject() throws Exception {
        initTaskId();
        ProcessDefinition processDefinition = executionService.getProcessDefinition(helper.getAuthorizedPerformerSubject(), taskId);
        assertEquals("process definition name not equal to deployed", WfServiceTestHelper.VALID_PROCESS_NAME, processDefinition.getName());
    }

    ///////////////////rask

    public void testGetProcessDefinitionByUnauthorizedSubject() throws Exception {
        try {
            executionService.getProcessDefinition(helper.getUnauthorizedPerformerSubject(), taskId);
            assertTrue("testGetProcessDefinitionByUnauthorizedSubject, no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessDefinitionByAuthorizedSubjectWithoutREADPermission() throws Exception {
        try {
            Collection<Permission> nullPermissions = Lists.newArrayList();
            helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(nullPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
            executionService.getProcessDefinition(helper.getAuthorizedPerformerSubject(), taskId);
            assertTrue("testGetProcessDefinitionByAuthorizedSubjectWithoutREADPermission, no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessDefinitionByFakeSubject() throws Exception {
        initTaskId();
        try {
            executionService.getProcessDefinition(helper.getFakeSubject(), taskId);
            assertTrue("testGetProcessDefinitionByFakeSubject, no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessDefinitionByAuthorizedSubjectWithInvalidTaskId() throws Exception {
        initTaskId();
        try {
            executionService.getProcessDefinition(helper.getAuthorizedPerformerSubject(), -1l);
            assertTrue("testGetProcessDefinitionByAuthorizedSubjectWithInvalidTaskId, no TaskDoesNotExistException", false);
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testGetProcessDefinitionByNullSubject() throws Exception {
        initTaskId();
        try {
            executionService.getProcessDefinition(null, taskId);
            assertTrue("testGetProcessDefinitionByNullSubject, no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }
}
