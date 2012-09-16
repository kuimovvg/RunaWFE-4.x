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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.service.AuthenticationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

/**
 */
public class AuthenticationServiceDelegateGetActorTest extends ServletTestCase {

    private static final String PREFIX = AuthenticationServiceDelegateGetActorTest.class.getName();

    private ServiceTestHelper th;

    private AuthenticationService authenticationService;

    public static Test suite() {
        return new TestSuite(AuthenticationServiceDelegateGetActorTest.class);
    }

    protected void setUp() throws Exception {
        th = new ServiceTestHelper(PREFIX);
        authenticationService = DelegateFactory.getInstance().getAuthenticationService();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        authenticationService = null;
        super.tearDown();
    }

    public void testNullSubject() throws Exception {
        try {
            authenticationService.getActor(null);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testFakeSubject() throws Exception {
        try {
            authenticationService.getActor(th.getFakeSubject());
        } catch (AuthenticationException e) {
        }
    }

    public void testGetActor() throws Exception {
        Actor actual = authenticationService.getActor(th.getAuthorizedPerformerSubject());
        assertEquals("Actors differ", th.getAuthorizedPerformerActor(), actual);
    }
}
