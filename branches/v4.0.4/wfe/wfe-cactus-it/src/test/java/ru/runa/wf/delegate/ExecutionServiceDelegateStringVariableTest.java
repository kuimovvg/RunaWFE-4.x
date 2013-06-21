package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

public class ExecutionServiceDelegateStringVariableTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(DefinitionPermission.START_PROCESS,
                DefinitionPermission.READ_STARTED_PROCESS);
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
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, variables);
        {
            String varName = "variable";
            String varValue = "";
            for (int i = 0; i < 300; ++i) {
                varValue = varValue + "-";
            }
            variables.put(varName, varValue);
        }
        List<WfTask> tasks = executionService.getTasks(helper.getAdminUser(),
                BatchPresentationFactory.TASKS.createDefault());
        executionService.completeTask(helper.getAdminUser(), tasks.get(0).getId(), variables, null);
    }
}
