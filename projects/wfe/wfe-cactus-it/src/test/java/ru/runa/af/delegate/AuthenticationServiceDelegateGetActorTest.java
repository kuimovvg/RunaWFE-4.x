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
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

public class AuthenticationServiceDelegateGetActorTest extends ServletTestCase {

    private static final String PREFIX = AuthenticationServiceDelegateGetActorTest.class.getName();

    private ServiceTestHelper th;

    private AuthenticationService authenticationService;

    public static Test suite() {
        return new TestSuite(AuthenticationServiceDelegateGetActorTest.class);
    }

    protected void setUp() throws Exception {
        th = new ServiceTestHelper(PREFIX);
        authenticationService = Delegates.getAuthenticationService();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        authenticationService = null;
        super.tearDown();
    }

    /* TODO: remove AuthenticationServiceDelegateGetActorTest?
    public void testNullUser() throws Exception {
        try {
            authenticationService.getActor(null);
        } catch (NullPointerException e) {
        }
    }
    */

    public void testFakeSubject() throws Exception {
        try {
            th.getFakeUser().getActor();
        } catch (AuthenticationException e) {
        }
    }

    public void testGetActor() throws Exception {
        Actor actual = th.getAuthorizedPerformerUser().getActor();
        assertEquals("Actors differ", th.getAuthorizedPerformerActor(), actual);
    }
}
