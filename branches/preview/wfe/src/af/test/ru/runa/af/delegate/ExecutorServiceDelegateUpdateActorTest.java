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

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * Created on 29.10.2004
 */
public class ExecutorServiceDelegateUpdateActorTest extends ServletTestCase {
    private ServiceTestHelper th;
    private ExecutorService executorService;
    private static String testPrefix = ExecutorServiceDelegateUpdateActorTest.class.getName();
    private Actor actor;
    private Actor newActor;
    private Actor returnedBaseGroupActor;
    private Map<String, Executor> executorsMap;
    private final List<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateUpdateActorTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, actor);

        newActor = new Actor("additionalActor", "Additional Actor", "Description", System.currentTimeMillis());

        actor = executorService.getActor(th.getAdminSubject(), actor.getId());

        super.setUp();
    }

    public void testUpdateActorByAuthorizedPerformer() throws Exception {
        returnedBaseGroupActor = executorService.update(th.getAuthorizedPerformerSubject(), actor, newActor);
        assertEquals("actor Name retuned by buisnessDelegete differes with expected", newActor.getName(), returnedBaseGroupActor.getName());
        assertEquals("actor Discription retuned by buisnessDelegete differes with expected", newActor.getDescription(), returnedBaseGroupActor
                .getDescription());
    }

    public void testUpdateActorByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.update(th.getUnauthorizedPerformerSubject(), actor, newActor);
            assertTrue("No exception - UpdateActorByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // This is supposed result of operation
        }
    }

    public void testUpdateNullActorByAuthorizedPerformer() throws Exception {
        Actor nullActor = null;
        try {
            executorService.update(th.getAuthorizedPerformerSubject(), actor, nullActor);
            assertTrue("No exception - UpdateNullActorByAuthorizedPerformer", false);
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
    }

    public void testUpdateActorWithNullSubject() throws Exception {
        try {
            executorService.update(null, actor, newActor);
            assertTrue("No exception - UpdateActorWithNullSubject", false);
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        actor = null;
        newActor = null;
        returnedBaseGroupActor = null;
        executorsMap = null;
        super.tearDown();
    }

}
