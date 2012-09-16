package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

public class ExecutionServiceDelegateStringVariableTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateStringVariableTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testLongVariables() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        {
            String varName = "variable";
            String varValue = "";
            for (int i = 0; i < 200; ++i) {
                varValue = varValue + "-";
            }
            variables.put(varName, varValue);
        }
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME, variables);
        {
            String varName = "variable";
            String varValue = "";
            for (int i = 0; i < 300; ++i) {
                varValue = varValue + "-";
            }
            variables.put(varName, varValue);
        }
        List<TaskStub> tasks = executionService.getTasks(helper.getAdminSubject(), WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        executionService.completeTask(helper.getAdminSubject(), tasks.get(0).getId(), tasks.get(0).getName(), helper.getAdministrator().getId(), variables);
    }
}
