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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariableDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S    
 */
public class DefinitionServiceDelegateGetFormTest extends ServletTestCase {

    private final static String STATE_1_NAME = "evaluating";

    private final static String STATE_1_TYPE = "html";

    private final static String STATE_1_FILE_NAME = "forms/evaluating.form";

    private final static String STATE_2_NAME = "notify";

    private final static String STATE_2_TYPE = "swt";

    private final static String STATE_2_FILE_NAME = "forms/notify.erp.form";

    final String VARIABLE_DEFAULT_FORMAT = "ru.runa.bpm.web.formgen.format.DefaultFormat";

    final String VARIABLE_DOUBLE_FORMAT = "ru.runa.bpm.web.formgen.format.DoubleFormat";

    private ExecutionService executionService;

    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private long taskId;
    private String taskName;

    protected static final long FAKE_ID = -1;
    protected static final String FAKE_NAME = "FAKE NAME OF TASK";

    public static Test suite() {
        return new TestSuite(DefinitionServiceDelegateGetFormTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = DelegateFactory.getInstance().getDefinitionService();
        executionService = DelegateFactory.getInstance().getExecutionService();

        definitionService.deployProcessDefinition(helper.getAdminSubject(), WfServiceTestHelper
                .readBytesFromFile(WfServiceTestHelper.ONE_SWIMLANE_FILE_NAME), Lists.newArrayList("testProcess"));

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);

        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);
        //		TaskStub taskStub =executionDelegate.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation())[0]; 
        //		taskId = taskStub.getId();
        //		taskName = taskStub.getName();

        super.setUp();
    }

    private void initTaskData() throws AuthorizationException, AuthenticationException {
        List<TaskStub> tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        taskId = tasks.get(0).getId();
        taskName = tasks.get(0).getName();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetFormTestByAuthorizedSubject() throws Exception {
        initTaskData();
        Interaction interaction = definitionService.getTaskInteraction(helper.getAuthorizedPerformerSubject(), taskId, taskName);
        // TODO : xml read from forms.xml & processdefinition.xml
        assertEquals("form name differ from original", STATE_1_NAME, interaction.getStateName());
        assertEquals("form name differ from original", STATE_1_TYPE, interaction.getType());

    }

    public void testGetFormTestByUnauthorizedSubject() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(helper.getUnauthorizedPerformerSubject(), taskId, taskName);
            fail("testGetFormTestByUnauthorizedSubject , no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetFormTestByNullSubject() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(null, taskId, taskName);
            fail("testGetFormTestByNullSubject , no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetFormTestByFakeSubject() throws Exception {
        initTaskData();
        try {
            taskId = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation()).get(0).getId();
            definitionService.getTaskInteraction(helper.getFakeSubject(), taskId, taskName);
            fail("testGetFormTestByFakeSubject , no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetFormTestByAuthorizedSubjectWithInvalidDefinitionId() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(helper.getAuthorizedPerformerSubject(), -1l, taskName);
            fail("testGetFormTestByAuthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testGetFormTestByUnauthorizedSubjectWithInvalidTaskId() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(helper.getUnauthorizedPerformerSubject(), -1l, taskName);
            fail("testGetFormTestByUnauthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testGetFormTestByUnauthorizedSubjectWithInvalidTaskName() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(helper.getUnauthorizedPerformerSubject(), taskId, taskName + "fake name");
            fail("testGetFormTestByUnauthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testCheckForm() throws Exception {
        List<TaskStub> tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation());
        assertEquals(tasks.size() > 0, true);

        Interaction interaction = definitionService.getTaskInteraction(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName());

        assertEquals("state name differs from expected", STATE_1_NAME, interaction.getStateName());
        assertEquals("state type differs from expected", STATE_1_TYPE, interaction.getType());

        Map<String, VariableDefinition> variableDefinitions = interaction.getVariables();
        assertEquals("state variables count differs from expected", variableDefinitions.size(), 5);
        VariableDefinition var = variableDefinitions.get("requester");
        assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormat());
        //assertFalse("optional variable flag was set to false", var.isOptional());

        var = (VariableDefinition) variableDefinitions.get("reason");
        assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormat());
        //assertTrue("optional variable flag was set to true", var.isOptional());

        var = (VariableDefinition) variableDefinitions.get("amount.asked");
        assertEquals("variable format differs from expected", VARIABLE_DOUBLE_FORMAT, var.getFormat());
        //assertFalse("optional variable flag was set to false", var.isOptional());

        var = (VariableDefinition) variableDefinitions.get("amount.granted");
        assertEquals("variable format differs from expected", VARIABLE_DOUBLE_FORMAT, var.getFormat());
        //assertFalse("optional variable flag was set to false", var.isOptional());

        var = (VariableDefinition) variableDefinitions.get("approved");
        assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormat());
        //assertTrue("optional variable flag was set to true", var.isOptional());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation());
        interaction = definitionService.getTaskInteraction(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName());

        assertEquals("state name differs from expected", STATE_2_NAME, interaction.getStateName());
        assertEquals("state type differs from expected", STATE_2_TYPE, interaction.getType());

        variableDefinitions = interaction.getVariables();
        assertEquals("state variables count differs from expected", variableDefinitions.size(), 1);

        var = (VariableDefinition) variableDefinitions.get("approved");
        assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormat());
        //assertTrue("optional variable flag was set to true", var.isOptional());
    }

}
