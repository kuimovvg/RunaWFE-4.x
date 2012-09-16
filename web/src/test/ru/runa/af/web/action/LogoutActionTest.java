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

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.InvalidSessionException;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;

import com.google.common.collect.Lists;

/**
 */
public class LogoutActionTest extends StrutsTestCase {
    private final static String testPrefix = LogoutActionTest.class.getName();

    private static final String SUCCESS = "/start.do";

    private LogoutAction action;

    private ActionMapping mapping;

    private ActionForm form;

    public static Test suite() {
        return new TestSuite(LogoutActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new LogoutAction();

        mapping = new ActionMapping();
        mapping.addForwardConfig(new ActionForward(Resources.FORWARD_SUCCESS, SUCCESS, false));

        form = null;

        List<Permission> systemP = Lists.newArrayList(Permission.READ, SystemPermission.LOGIN_TO_SYSTEM);
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(systemP);
    }

    protected void tearDown() throws Exception {
        testHelper.releaseResources();
        testHelper = null;
    }

    public void testLogoutAction() {
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("LogoutAction returns null forward", forward);
        assertEquals("LogoutAction returns wrong forward", SUCCESS, forward.getPath());

        try {
            SubjectHttpSessionHelper.getActorSubject(session);
            fail("Logout action does not clean session attribute");
        } catch (InvalidSessionException e) {
        }
        try {
            ProfileHttpSessionHelper.getProfile(session);
            fail("Logout action does not clean session attribute");
        } catch (InvalidSessionException e) {
        }
    }

    public void testLogoutActionUnauthorized() {
        SubjectHttpSessionHelper.removeActorSubject(session);
        ProfileHttpSessionHelper.removeProfile(session);

        try {
            action.execute(mapping, form, request, response);
            fail("Successfuly logout unauthorized user");
        } catch (InvalidSessionException e) {
        }
    }

    public String getTestPrefix() {
        return testPrefix;
    }

}
