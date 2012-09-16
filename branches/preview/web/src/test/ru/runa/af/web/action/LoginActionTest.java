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
package ru.runa.af.web.action;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Actor;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.WebServiceTestHelper;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.LoginForm;
import ru.runa.common.web.InvalidSessionException;
import ru.runa.common.web.Messages;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;

import com.google.common.collect.Lists;

/**
 */
public class LoginActionTest extends StrutsTestCase {
    private final static String testPrefix = LoginActionTest.class.getName();

    private static final String FAILURE = "/start.do";

    private static final String SUCCESS = "/manage_tasks.do";

    private LoginAction loginAction;

    private ActionMapping mapping;

    private LoginForm form;

    public static Test suite() {
        return new TestSuite(LoginActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        loginAction = new LoginAction();

        mapping = new ActionMapping();
        mapping.addForwardConfig(new ActionForward(Resources.FORWARD_SUCCESS, SUCCESS, false));
        mapping.addForwardConfig(new ActionForward(Resources.FORWARD_FAILURE, FAILURE, false));

        form = new LoginForm();
        form.reset(mapping, request);

        List<Permission> systemP = Lists.newArrayList(Permission.READ, SystemPermission.LOGIN_TO_SYSTEM);
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(systemP);
    }

    public void testLoginActionUnauthorized() throws Exception {
        form.setLogin(testPrefix + WebServiceTestHelper.UNAUTHORIZED_PERFORMER_NAME);
        form.setPassword(WebServiceTestHelper.UNAUTHORIZED_PERFORMER_PASSWORD);

        SubjectHttpSessionHelper.removeActorSubject(session);
        ProfileHttpSessionHelper.removeProfile(session);

        ActionForward forward = loginAction.execute(mapping, form, request, response);
        assertNotNull("LoginAction returns null forward", forward);
        assertEquals("LoginAction returns wrong forward", FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("LoginAction returns with errors", messages);
        assertEquals("LoginAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());
        try {
            SubjectHttpSessionHelper.getActorSubject(session);
            fail("Login action created session attribute");
        } catch (InvalidSessionException e) {
        }
        try {
            ProfileHttpSessionHelper.getProfile(session);
            fail("Login action created session attribute");
        } catch (InvalidSessionException e) {
        }
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAuthorizedPerformerSubject(), session);
        ProfileHttpSessionHelper.setProfile(testHelper.getDefaultProfile(testHelper.getAuthorizedPerformerSubject()), session);
    }

    public void testLoginActionWrongSystemName() throws Exception {
        form.setLogin(testPrefix + WebServiceTestHelper.UNAUTHORIZED_PERFORMER_NAME);
        form.setPassword(WebServiceTestHelper.UNAUTHORIZED_PERFORMER_PASSWORD);

        SubjectHttpSessionHelper.removeActorSubject(session);
        ProfileHttpSessionHelper.removeProfile(session);

        ActionForward forward = loginAction.execute(mapping, form, request, response);
        assertNotNull("LoginAction returns null forward", forward);
        assertEquals("LoginAction returns wrong forward", FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("LoginAction returns with errors", messages);
        assertEquals("LoginAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());
        try {
            SubjectHttpSessionHelper.getActorSubject(session);
            fail("Login action created session attribute");
        } catch (InvalidSessionException e) {
        }
        try {
            ProfileHttpSessionHelper.getProfile(session);
            fail("Login action created session attribute");
        } catch (InvalidSessionException e) {
        }
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAuthorizedPerformerSubject(), session);
        ProfileHttpSessionHelper.setProfile(testHelper.getDefaultProfile(testHelper.getAuthorizedPerformerSubject()), session);
    }

    public void testLoginAction() throws Exception {
        form.setLogin(testPrefix + WebServiceTestHelper.AUTHORIZED_PERFORMER_NAME);
        form.setPassword(WebServiceTestHelper.AUTHORIZED_PERFORMER_PASSWORD);

        ActionForward forward = loginAction.execute(mapping, form, request, response);
        assertNotNull("LoginAction returns null forward", forward);
        assertEquals("LoginAction returns wrong forward", SUCCESS, forward.getPath());
        assertNull("LoginAction returns with errors", getGlobalErrors());
    }

    public void testLoginActionTwoActorsWithSamePassword() throws Exception {
        Actor secundActor = testHelper.createActor(testPrefix + WebServiceTestHelper.AUTHORIZED_PERFORMER_NAME + "2", "same password");
        testHelper.getExecutorService().setPassword(testHelper.getAdminSubject(), secundActor,
                WebServiceTestHelper.AUTHORIZED_PERFORMER_PASSWORD);

        form.setLogin(testPrefix + WebServiceTestHelper.AUTHORIZED_PERFORMER_NAME);
        form.setPassword(WebServiceTestHelper.AUTHORIZED_PERFORMER_PASSWORD);

        ActionForward forward = loginAction.execute(mapping, form, request, response);
        assertNotNull("LoginAction returns null forward", forward);
        assertEquals("LoginAction returns wrong forward", SUCCESS, forward.getPath());
        assertNull("LoginAction returns with errors", getGlobalErrors());
    }

    public void testLoginActionSessionVariables() throws Exception {
        form.setLogin(testPrefix + WebServiceTestHelper.AUTHORIZED_PERFORMER_NAME);
        form.setPassword(WebServiceTestHelper.AUTHORIZED_PERFORMER_PASSWORD);

        loginAction.execute(mapping, form, request, response);
        assertNotNull("session does not contain subject", SubjectHttpSessionHelper.getActorSubject(session));
        assertNotNull("session does not contain subject", ProfileHttpSessionHelper.getProfile(session));
    }

    public String getTestPrefix() {
        return testPrefix;
    }
}
