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
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.VariableDefinition;

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

    private final static String STATE_2_NAME = "notify";

    private final static String STATE_2_TYPE = "swt";

    final String VARIABLE_DEFAULT_FORMAT = "ru.runa.bpm.web.formgen.format.DefaultFormat";

    final String VARIABLE_DOUBLE_FORMAT = "ru.runa.bpm.web.formgen.format.DoubleFormat";

    private ExecutionService executionService;

    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private long taskId;

    protected static final long FAKE_ID = -1;
    protected static final String FAKE_NAME = "FAKE NAME OF TASK";

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();
        executionService = Delegates.getExecutionService();

        definitionService.deployProcessDefinition(helper.getAdminUser(),
                WfServiceTestHelper.readBytesFromFile(WfServiceTestHelper.ONE_SWIMLANE_FILE_NAME), Lists.newArrayList("testProcess"));

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);

        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME, null);
        super.setUp();
    }

    private void initTaskData() throws AuthorizationException, AuthenticationException {
        List<WfTask> tasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), helper.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        taskId = tasks.get(0).getId();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetFormTestByAuthorizedSubject() throws Exception {
        initTaskData();
        Interaction interaction = definitionService.getTaskInteraction(helper.getAuthorizedPerformerUser(), taskId);
        // TODO assertEquals("form name differ from original", STATE_1_NAME,
        // interaction.getStateName());
        // TODO assertEquals("form name differ from original", STATE_1_TYPE,
        // interaction.getType());

    }

    public void testGetFormTestByUnauthorizedSubject() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(helper.getUnauthorizedPerformerUser(), taskId);
            fail("testGetFormTestByUnauthorizedSubject , no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetFormTestByNullUser() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(null, taskId);
            fail("testGetFormTestByNullSubject , no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetFormTestByFakeSubject() throws Exception {
        initTaskData();
        try {
            taskId = executionService.getTasks(helper.getAuthorizedPerformerUser(), helper.getTaskBatchPresentation()).get(0).getId();
            definitionService.getTaskInteraction(helper.getFakeUser(), taskId);
            fail("testGetFormTestByFakeSubject , no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetFormTestByAuthorizedSubjectWithInvalidDefinitionId() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(helper.getAuthorizedPerformerUser(), -1l);
            fail("testGetFormTestByAuthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testGetFormTestByUnauthorizedSubjectWithInvalidTaskId() throws Exception {
        initTaskData();
        try {
            definitionService.getTaskInteraction(helper.getUnauthorizedPerformerUser(), -1l);
            fail("testGetFormTestByUnauthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testCheckForm() throws Exception {
        List<WfTask> tasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), helper.getTaskBatchPresentation());
        assertEquals(tasks.size() > 0, true);

        Interaction interaction = definitionService.getTaskInteraction(helper.getAuthorizedPerformerUser(), tasks.get(0).getId());

        // TODO assertEquals("state name differs from expected", STATE_1_NAME,
        // interaction.getStateName());
        if (false) {
            assertEquals("state type differs from expected", STATE_1_TYPE, interaction.getType());

            Map<String, VariableDefinition> variableDefinitions = interaction.getVariables();
            assertEquals("state variables count differs from expected", variableDefinitions.size(), 5);
            VariableDefinition var = variableDefinitions.get("requester");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertFalse("optional variable flag was set to false",
            // var.isOptional());

            var = variableDefinitions.get("reason");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertTrue("optional variable flag was set to true",
            // var.isOptional());

            var = variableDefinitions.get("amount.asked");
            assertEquals("variable format differs from expected", VARIABLE_DOUBLE_FORMAT, var.getFormatClassName());
            // assertFalse("optional variable flag was set to false",
            // var.isOptional());

            var = variableDefinitions.get("amount.granted");
            assertEquals("variable format differs from expected", VARIABLE_DOUBLE_FORMAT, var.getFormatClassName());
            // assertFalse("optional variable flag was set to false",
            // var.isOptional());

            var = variableDefinitions.get("approved");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertTrue("optional variable flag was set to true",
            // var.isOptional());

            executionService.completeTask(helper.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>(), null);

            tasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), helper.getTaskBatchPresentation());
            interaction = definitionService.getTaskInteraction(helper.getAuthorizedPerformerUser(), tasks.get(0).getId());

            // TODO assertEquals("state name differs from expected",
            // STATE_2_NAME, interaction.getStateName());
            fail("getStateName");
            assertEquals("state type differs from expected", STATE_2_TYPE, interaction.getType());

            variableDefinitions = interaction.getVariables();
            assertEquals("state variables count differs from expected", variableDefinitions.size(), 1);

            var = variableDefinitions.get("approved");
            assertEquals("variable format differs from expected", VARIABLE_DEFAULT_FORMAT, var.getFormatClassName());
            // assertTrue("optional variable flag was set to true",
            // var.isOptional());
        }
    }

}
