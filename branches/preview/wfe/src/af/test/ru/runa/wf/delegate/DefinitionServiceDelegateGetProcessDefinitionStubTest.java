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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
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
public class DefinitionServiceDelegateGetProcessDefinitionStubTest extends ServletTestCase {
    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    public static Test suite() {
        return new TestSuite(DefinitionServiceDelegateGetProcessDefinitionStubTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = DelegateFactory.getInstance().getDefinitionService();

        helper.deployValidProcessDefinition();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetProcessDefinitionStubByAuthorizedSubject() throws Exception {
        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        ProcessDefinition process = definitionService.getLatestProcessDefinitionStub(helper.getAuthorizedPerformerSubject(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        long processId = process.getNativeId();
        ProcessDefinition actualProcess = definitionService.getProcessDefinitionStub(helper.getAuthorizedPerformerSubject(), processId);
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getName(), actualProcess
                .getName());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getId(), actualProcess
                .getId());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getNativeId(),
                actualProcess.getNativeId());
    }

    public void testGetProcessDefinitionStubByAuthorizedSubjectWithoutREADPermission() throws Exception {
        Collection<Permission> permissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        try {
            definitionService.getLatestProcessDefinitionStub(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetProcessDefinitionStubByAuthorizedSubjectWithourREADPermission(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessDefinitionStubByUnauthorizedSubject() throws Exception {
        try {
            definitionService.getLatestProcessDefinitionStub(helper.getUnauthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetProcessDefinitionStubByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessDefinitionStubByFakeSubject() throws Exception {
        try {
            definitionService.getLatestProcessDefinitionStub(helper.getFakeSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetProcessDefinitionStubByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessDefinitionStubByNullSubject() throws Exception {
        try {
            definitionService.getLatestProcessDefinitionStub(null, WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetProcessDefinitionStubByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
}
