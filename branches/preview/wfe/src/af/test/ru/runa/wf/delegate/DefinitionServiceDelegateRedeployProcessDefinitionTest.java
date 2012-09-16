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

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateRedeployProcessDefinitionTest extends ServletTestCase {

    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private long processDefinitionId;

    public static Test suite() {
        return new TestSuite(DefinitionServiceDelegateRedeployProcessDefinitionTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = DelegateFactory.getInstance().getDefinitionService();

        helper.deployValidProcessDefinition();

        processDefinitionId = definitionService.getLatestProcessDefinitionStub(helper.getAdminSubject(), WfServiceTestHelper.VALID_PROCESS_NAME)
                .getNativeId();

        Collection<Permission> redeployPermissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.REDEPLOY_DEFINITION);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(redeployPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testRedeployProcessByAuthorizedPerformer() throws Exception {
        definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerSubject(), processDefinitionId, helper.getValidProcessDefinition(),
                Lists.newArrayList("testProcess"));
        List<ProcessDefinition> deployedProcesses = definitionService.getLatestProcessDefinitionStubs(helper.getAuthorizedPerformerSubject(), helper
                .getProcessDefinitionBatchPresentation());
        if (deployedProcesses.size() != 1) {
            assertTrue("testRedeployProcessByAuthorizedPerformer wrongNumberOfProcessDefinitions", false);
        }
        if (!deployedProcesses.get(0).getName().equals(WfServiceTestHelper.VALID_PROCESS_NAME)) {
            assertTrue("testRedeployProcessByAuthorizedPerformer wrongNameOfDeployedProcessDefinitions", false);
        }
    }

    public void testRedeployProcessByAuthorizedPerformerWithoutREDEPLOYPermission() throws Exception {
        Collection<Permission> nullPermissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(nullPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerSubject(), processDefinitionId, helper
                    .getValidProcessDefinition(), Lists.newArrayList("testProcess" ));
            assertTrue("definitionDelegate.redeployProcessByAuthorizedPerformer() no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testRedeployProcessByUnauthorizedPerformer() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getUnauthorizedPerformerSubject(), processDefinitionId, helper
                    .getValidProcessDefinition(), Lists.newArrayList("testProcess"));
            assertTrue("definitionDelegate.redeployProcessByUnauthorizedPerformer() no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testRedeployProcessWithNullSubject() throws Exception {
        try {
            definitionService.redeployProcessDefinition(null, processDefinitionId, helper.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            assertTrue("testRedeployProcessWithNullSubject no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testRedeployProcessWithFakeSubject() throws Exception {
        try {
            Subject fakeSubject = helper.getFakeSubject();
            definitionService.redeployProcessDefinition(fakeSubject, processDefinitionId, helper.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            assertTrue("testRedeployProcessWithFakeSubject no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testRedeployInvalidProcessByAuthorizedPerformer() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerSubject(), processDefinitionId, helper
                    .getInValidProcessDefinition(), Lists.newArrayList("testProcess"));
            assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer() no DefinitionParsingException", false);
        } catch (ProcessDefinitionArchiveException e) {
        }
    }

    public void testRedeployWithInvalidProcessId() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerSubject(), -1l, helper.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            fail("testRedeployWithInvalidProcessId() no Exception");
        } catch (ProcessDefinitionDoesNotExistException e) {
        }
    }

    public void testRedeployInvalidProcess() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerSubject(), processDefinitionId, helper
                    .getInValidProcessDefinition(), Lists.newArrayList("testProcess"));
            fail("testRedeployInvalidProcess() no Exception");
        } catch (ProcessDefinitionArchiveException e) {
        }
    }

}
