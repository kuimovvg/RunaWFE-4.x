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
package ru.runa.af.delegate;

import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.ASystem;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.af.service.SystemService;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * Created on 16.08.2004
 */
public class AASystemServiceDelegateLoginTest extends ServletTestCase {
    private ServiceTestHelper th;
    private SystemService systemService;
    private static String testPrefix = AASystemServiceDelegateLoginTest.class.getName();

    public static TestSuite suite() {
        return new TestSuite(AASystemServiceDelegateLoginTest.class);
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        systemService = null;
        super.tearDown();
    }

    protected void setUp() throws Exception {
        systemService = DelegateFactory.getInstance().getSystemService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        th.setPermissionsToAuthorizedPerformerOnSystem(Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM));
        super.setUp();
    }

    public void testLoginWithNullSubject() throws Exception {
        try {
            systemService.login(null, ASystem.SYSTEM);
            assertTrue("SystemServiceDelegate does not throw IllegalArgumentException on login(null) call.", false);
        } catch (IllegalArgumentException e) {
            //that's what we expected
        }
    }

    public void testLoginWithUnauthorizedPerformer() throws Exception {
        try {
            systemService.login(th.getUnauthorizedPerformerSubject(), ASystem.SYSTEM);
            assertTrue("SystemServiceDelegate does not throw AuthorizationFailedException on login() with unauthrozied performer subject call.",
                    false);
        } catch (AuthorizationException e) {
            //that's what we expected
        }
    }

    public void testLoginWithAuthorizedPerformer() throws Exception {
        systemService.login(th.getAuthorizedPerformerSubject(), ASystem.SYSTEM);
        assertTrue("SystemServiceDelegate.login() works.", true);
    }

    public void testLoginWithFakeSubject() throws Exception {
        try {
            systemService.login(th.getFakeSubject(), ASystem.SYSTEM);
            assertTrue("SystemServiceDelegate does not throw AuthorizationFailedException on login() with fakeSubject call.", false);
        } catch (AuthenticationException e) {
            //that's what we expected	
        }
    }

}
