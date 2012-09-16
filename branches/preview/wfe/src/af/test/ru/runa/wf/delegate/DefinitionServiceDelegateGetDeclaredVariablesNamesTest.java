package ru.runa.wf.delegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.WorkflowSystemPermission;
import ru.runa.wf.form.VariableDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * @author Pashkov Alexander
 */
public class DefinitionServiceDelegateGetDeclaredVariablesNamesTest extends ServletTestCase {

    private static final String DEFINITION_WITH_VARIABLES_XML = "processWithVariablesXml";
    private static final String DEFINITION_WITHOUT_VARIABLES_XML = "processWithoutVariablesXml";

    private DefinitionService definitionService = null;

    private WfServiceTestHelper helper = null;

    private long definitionWithVariablesXmlId;
    private long definitionWithoutVariablesXmlId;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = DelegateFactory.getInstance().getDefinitionService();

        Collection<Permission> deployPermissions = Lists.newArrayList(WorkflowSystemPermission.DEPLOY_DEFINITION);
        helper.setPermissionsToAuthorizedPerformerOnSystem(deployPermissions);
        definitionService.deployProcessDefinition(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper
                .readBytesFromFile(DEFINITION_WITH_VARIABLES_XML + ".par"), Lists.newArrayList("testProcess"));
        definitionService.deployProcessDefinition(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper
                .readBytesFromFile(DEFINITION_WITHOUT_VARIABLES_XML + ".par"), Lists.newArrayList("testProcess"));

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, DEFINITION_WITH_VARIABLES_XML);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, DEFINITION_WITHOUT_VARIABLES_XML);

        definitionWithVariablesXmlId = definitionService.getLatestProcessDefinitionStub(helper.getAuthorizedPerformerSubject(),
                DEFINITION_WITH_VARIABLES_XML).getNativeId();
        definitionWithoutVariablesXmlId = definitionService.getLatestProcessDefinitionStub(helper.getAuthorizedPerformerSubject(),
                DEFINITION_WITHOUT_VARIABLES_XML).getNativeId();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(DEFINITION_WITH_VARIABLES_XML);
        helper.undeployValidProcessDefinition(DEFINITION_WITHOUT_VARIABLES_XML);
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetDeclaredVariablesNamesByAuthorizedSubject() {
        testImpl(helper.getAuthorizedPerformerSubject(), definitionWithVariablesXmlId, Lists.newArrayList("var1", "Var2", "var3"), null);
        testImpl(helper.getAuthorizedPerformerSubject(), definitionWithoutVariablesXmlId, new ArrayList<String>(), null);
    }

    public void testGetDeclaredVariablesNamesByUnauthorizedSubject() {
        //testImpl(helper.getUnauthorizedPerformerSubject(), definitionWithVariablesXmlId, null, AuthorizationException.class);
        //testImpl(helper.getUnauthorizedPerformerSubject(), definitionWithoutVariablesXmlId, null, AuthorizationException.class);
    }

    public void testGetDeclaredVariablesNamesByNullSubject() {
        //testImpl(null, definitionWithVariablesXmlId, null, IllegalArgumentException.class);
        //testImpl(null, definitionWithoutVariablesXmlId, null, IllegalArgumentException.class);
    }

    public void testGetDeclaredVariablesNamesByFakeSubject() {
//        testImpl(helper.getFakeSubject(), definitionWithVariablesXmlId, null, AuthenticationException.class);
//        testImpl(helper.getFakeSubject(), definitionWithoutVariablesXmlId, null, AuthenticationException.class);
    }

    public void testGetDeclaredVariablesNamesOnInvalidDefinitionId() {
        testImpl(helper.getAuthorizedPerformerSubject(), -1, null, ProcessDefinitionDoesNotExistException.class);
    }

    private void testImpl(Subject subject, long definitionId, List<String> expected, Class<? extends Exception> exception) {
        List<VariableDefinition> actual;
        try {
            actual = definitionService.getProcessDefinitionVariables(subject, definitionId);
        } catch (Exception e) {
            assertEquals(exception, e.getClass());
            return;
        }
        if (exception != null) {
            fail("exception expected");
        }

        List<String> actualNames = Lists.newArrayList();
        for (VariableDefinition var : actual) {
            actualNames.add(var.getName());
        }
        ArrayAssert.assertWeakEqualArrays("", expected, actualNames);
    }
}
