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

import java.util.Set;

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.ActorPrincipal;
import ru.runa.af.AuthenticationException;
import ru.runa.af.service.AuthenticationService;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;

public class AuthenticationServiceDelegatePasswordLoginModuleTest extends ServletTestCase {

    private static final String PREFIX = AuthenticationServiceDelegatePasswordLoginModuleTest.class.getName();

    private static final String ACTOR1_NAME = "actor1";

    private static final String ACTOR2_NAME = "actor2";

    private static final String ACTOR_VALID_PWD = "validPWD";

    private ServiceTestHelper th;

    private AuthenticationService authenticationService;

    private ExecutorService executorService;

    private Actor validActor;

    public static Test suite() {
        return new TestSuite(AuthenticationServiceDelegatePasswordLoginModuleTest.class);
    }

    protected void setUp() throws Exception {
        th = new ServiceTestHelper(PREFIX);
        authenticationService = th.getAuthenticationService();
        executorService = th.getExecutorService();

        validActor = th.createActorIfNotExist(PREFIX + ACTOR1_NAME, "");
        executorService.setPassword(th.getAdminSubject(), validActor, ACTOR_VALID_PWD);

        super.setUp();
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        authenticationService = null;
        executorService = null;
        validActor = null;
        super.tearDown();
    }

    private boolean hasActorPrincipal(Subject subject, Actor actor) {
        Set<ActorPrincipal> principals = subject.getPrincipals(ActorPrincipal.class);
        for (ActorPrincipal actorPrincipal : principals) {
            if (actorPrincipal.getActor().equals(actor)) {
                return true;
            }
        }
        return false;
    }

    public void testValidPassword() throws Exception {
        Subject validSubject = authenticationService.authenticate(validActor.getName(), ACTOR_VALID_PWD);
        assertTrue("authenticated subject doesn't contains actor principal", hasActorPrincipal(validSubject, validActor));
    }

    public void testValidPasswordWithAnotherActorWithSamePassword() throws Exception {
        Actor actor2 = th.createActorIfNotExist(PREFIX + ACTOR2_NAME, "");
        executorService.setPassword(th.getAdminSubject(), actor2, ACTOR_VALID_PWD);

        Subject validSubject = authenticationService.authenticate(validActor.getName(), ACTOR_VALID_PWD);
        assertTrue("authenticated subject doesn't contains actor principal", hasActorPrincipal(validSubject, validActor));
    }

    public void testLoginFakeActor() throws Exception {
        try {
            authenticationService.authenticate(th.getFakeActor().getName(), ACTOR_VALID_PWD);
            fail("allowing fake actor");
        } catch (AuthenticationException e) {
            // expected
        }
    }

    public void testInValidPassword() throws Exception {
        try {
            authenticationService.authenticate(validActor.getName(), ACTOR_VALID_PWD + "Invalid");
            fail("allowing invalid password");
        } catch (AuthenticationException e) {
            // expected
        }
    }

    public void testInValidLogin() throws Exception {
        try {
            authenticationService.authenticate(validActor.getName() + "Invalid", ACTOR_VALID_PWD);
            fail("allowing invalid login");
        } catch (AuthenticationException e) {
            // expected
        }
    }

    public void testNullPassword() throws Exception {
        try {
            authenticationService.authenticate(validActor.getName(), null);
            fail("allowing NULL password");
        } catch (AuthenticationException e) {
        }
    }
}
