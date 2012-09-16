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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariableDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetStartFormTest extends ServletTestCase {
    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private Long definitionId;

    public static Test suite() {
        return new TestSuite(DefinitionServiceDelegateGetStartFormTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = DelegateFactory.getInstance().getDefinitionService();

        helper.deployValidProcessDefinition();

        definitionId = definitionService.getLatestProcessDefinitionStub(helper.getAdminSubject(), WfServiceTestHelper.VALID_PROCESS_NAME)
                .getNativeId();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetStartFormTestByAuthorizedSubject() throws Exception {
        Interaction startForm = definitionService.getStartInteraction(helper.getAuthorizedPerformerSubject(), definitionId);

        /// TO DO : xml read from forms.xml & processdefinition.xml
        assertEquals("start form name differ from original", "request a payraise", startForm.getStateName());
        assertEquals("start form name differ from original", "html", startForm.getType());
        Map<String, VariableDefinition> vars = startForm.getVariables();
        List<String> actual = new ArrayList<String>();
        for (VariableDefinition var : vars.values()) {
            actual.add(var.getName());
        }

        List<String> expected = Lists.newArrayList("reason", "amount.asked", "time", "file", "actor");
        ArrayAssert.assertWeakEqualArrays("Variables from start from differ from declaration", expected, actual);
    }

    public void testGetStartFormTestByUnauthorizedSubject() throws Exception {
        try {
            definitionService.getStartInteraction(helper.getUnauthorizedPerformerSubject(), definitionId);
            fail("testGetStartFormTestByUnauthorizedSubject , no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetStartFormTestByFakeSubject() throws Exception {
        try {
            definitionService.getStartInteraction(helper.getFakeSubject(), definitionId);
            fail("testGetStartFormTestByFakeSubject , no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetStartFormTestByNullSubject() throws Exception {
        try {
            definitionService.getStartInteraction(null, definitionId);
            fail("testGetStartFormTestByNullSubject , no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetStartFormTestByAuthorizedSubjectWithInvalidDefinitionId() throws Exception {
        try {
            definitionService.getStartInteraction(helper.getAuthorizedPerformerSubject(), -1l);
            fail("testGetStartFormTestByAuthorizedSubjectWithInvalidDefinitionId , no Exception");
        } catch (ProcessDefinitionDoesNotExistException e) {
        }
    }
}
