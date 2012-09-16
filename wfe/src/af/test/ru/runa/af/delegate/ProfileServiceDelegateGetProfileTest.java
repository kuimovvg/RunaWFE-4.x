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

import ru.runa.af.AuthenticationException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.service.ProfileService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

public class ProfileServiceDelegateGetProfileTest extends ServletTestCase {
    private static final String PREFIX = ProfileServiceDelegateGetProfileTest.class.getName();

    private ServiceTestHelper th;

    private ProfileService profileService;

    public static Test suite() {
        return new TestSuite(ProfileServiceDelegateGetProfileTest.class);
    }

    protected void setUp() throws Exception {
        th = new ServiceTestHelper(PREFIX);
        profileService = DelegateFactory.getInstance().getProfileService();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        profileService = null;
        super.tearDown();
    }

    public void testNullSubject() throws Exception {
        try {
            profileService.getProfile(null);
            fail("ProfileServiceDelegate.saveProfile() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testFakeSubject() throws Exception {
        try {
            profileService.getProfile(th.getFakeSubject());
            fail("ProfileServiceDelegate.saveProfile() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testSaveProfile() throws Exception {
        profileService.saveProfile(th.getAuthorizedPerformerSubject(), th.getDefaultProfile(th.getAuthorizedPerformerSubject()));
        Profile actual = profileService.getProfile(th.getAuthorizedPerformerSubject());
        assertEquals("saved and actual profiles differ", th.getDefaultProfile(th.getAuthorizedPerformerSubject()), actual);
    }

}
