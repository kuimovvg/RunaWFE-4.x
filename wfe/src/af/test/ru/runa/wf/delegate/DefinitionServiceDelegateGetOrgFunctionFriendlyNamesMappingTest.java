package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.WorkflowSystemPermission;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * @author Pashkov Alexander
 */
public class DefinitionServiceDelegateGetOrgFunctionFriendlyNamesMappingTest extends ServletTestCase {

    private static final String DEFINITION_WITH_MAPPING = "processWithOrgFunctionsMapping";
    private static final String DEFINITION_WITHOUT_MAPPING = "processWithoutOrgFunctionsMapping";

    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private long definitionWithMappingId;
    private long definitionWithoutMappingId;

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = DelegateFactory.getInstance().getDefinitionService();

        Collection<Permission> deployPermissions = Lists.newArrayList(WorkflowSystemPermission.DEPLOY_DEFINITION);
        helper.setPermissionsToAuthorizedPerformerOnSystem(deployPermissions);
        definitionService.deployProcessDefinition(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper
                .readBytesFromFile(DEFINITION_WITH_MAPPING + ".par"), Lists.newArrayList("testProcess"));
        definitionService.deployProcessDefinition(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper
                .readBytesFromFile(DEFINITION_WITHOUT_MAPPING + ".par"), Lists.newArrayList("testProcess"));

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, DEFINITION_WITH_MAPPING);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, DEFINITION_WITHOUT_MAPPING);

        definitionWithMappingId = definitionService.getLatestProcessDefinitionStub(helper.getAuthorizedPerformerSubject(), DEFINITION_WITH_MAPPING)
                .getNativeId();
        definitionWithoutMappingId = definitionService.getLatestProcessDefinitionStub(helper.getAuthorizedPerformerSubject(),
                DEFINITION_WITHOUT_MAPPING).getNativeId();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(DEFINITION_WITH_MAPPING);
        helper.undeployValidProcessDefinition(DEFINITION_WITHOUT_MAPPING);
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetOrgFunctionFriendlyNamesMappingByAuthorizedSubject() {
        Map<String, String> expected = new HashMap<String, String>();
        testImpl(helper.getAuthorizedPerformerSubject(), definitionWithoutMappingId, expected, null);
        expected.put("ru.runa.af.organizationfunction.ExecutorByNameFunction", "ExecutorByNameFunction");
        testImpl(helper.getAuthorizedPerformerSubject(), definitionWithMappingId, expected, null);
    }

    public void testGetOrgFunctionFriendlyNamesMappingByUnauthorizedSubject() {
        testImpl(helper.getUnauthorizedPerformerSubject(), definitionWithMappingId, null, AuthorizationException.class);
        testImpl(helper.getUnauthorizedPerformerSubject(), definitionWithoutMappingId, null, AuthorizationException.class);
    }

    public void testGetOrgFunctionFriendlyNamesMappingByNullSubject() {
        testImpl(null, definitionWithMappingId, null, IllegalArgumentException.class);
        testImpl(null, definitionWithoutMappingId, null, IllegalArgumentException.class);
    }

    public void testGetOrgFunctionFriendlyNamesMappingByFakeSubject() {
        testImpl(helper.getFakeSubject(), definitionWithMappingId, null, AuthenticationException.class);
        testImpl(helper.getFakeSubject(), definitionWithoutMappingId, null, AuthenticationException.class);
    }

    public void testGetOrgFunctionFriendlyNamesMappingOnInvalidDefinitionId() {
        testImpl(helper.getAuthorizedPerformerSubject(), -1, null, ProcessDefinitionDoesNotExistException.class);
    }

    private void testImpl(Subject subject, long definitionId, Map<String, String> expected, Class<? extends Exception> exception) {
        Map<String, String> mapping;
        try {
            mapping = definitionService.getOrgFunctionFriendlyNamesMapping(subject, definitionId);
        } catch (Exception e) {
            assertEquals(exception, e.getClass());
            return;
        }
        if (exception != null) {
            fail("exception expected");
        }

        assertNotNull(mapping);
        assertEquals(expected.size(), mapping.size());
        for (Iterator<Map.Entry<String, String>> iter = expected.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, String> next = iter.next();
            String actualValue = mapping.get(next.getKey());
            assertNotNull(actualValue);
            assertEquals(next.getValue(), actualValue);
        }
    }
}
