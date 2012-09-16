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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.WfServiceTestHelper;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetLatestProcessDefinitionsStubsTest extends ServletTestCase {
    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(DefinitionServiceDelegateGetLatestProcessDefinitionsStubsTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = DelegateFactory.getInstance().getDefinitionService();

        helper.deployValidProcessDefinition();
        batchPresentation = helper.getProcessDefinitionBatchPresentation();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        definitionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetLatestProcessDefinitionsStubsByAuthorizedSubject() throws Exception {
        List<ProcessDefinition> processes = definitionService.getLatestProcessDefinitionStubs(helper.getAuthorizedPerformerSubject(),
                batchPresentation);

        assertEquals("definitionDelegate.getLatestDefinitionStub() returned not expected list", 1, processes.size());
        assertEquals("definitionDelegate.getLatestDefinitionStub() returned process with different name", processes.get(0).getName(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testGetLatestProcessDefinitionsStubsByUnauthorizedSubject() throws Exception {
        List<ProcessDefinition> processes;
        try {
            processes = definitionService.getLatestProcessDefinitionStubs(helper.getUnauthorizedPerformerSubject(), batchPresentation);
            assertEquals("testGetLatestDefinitionStubByUnauthorizedSubject returns process definition for unauthorized performer", 0,
                    processes.size());
        } catch (AuthorizationException e) {
        }
    }

    public void testGetLatestProcessDefinitionsStubsByFakeSubject() throws Exception {
        try {
            definitionService.getLatestProcessDefinitionStubs(helper.getFakeSubject(), batchPresentation);
            assertTrue("testGetLatestDefinitionStubByUnauthorizedSubject, no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testGetLatestProcessDefinitionsStubsByNullSubject() throws Exception {
        try {
            definitionService.getLatestProcessDefinitionStubs(null, batchPresentation);
            assertTrue("testGetLatestProcessDefinitionsStubsByNullSubject, no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }
}
