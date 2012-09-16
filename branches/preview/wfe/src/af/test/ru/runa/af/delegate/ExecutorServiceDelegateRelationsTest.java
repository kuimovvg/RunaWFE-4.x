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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.Relation;
import ru.runa.af.RelationPair;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.RelationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

public class ExecutorServiceDelegateRelationsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private RelationService relationService;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateRelationsTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        relationService = DelegateFactory.getInstance().getRelationService();
        th = new ServiceTestHelper(ExecutorServiceDelegateRelationsTest.class.getName());
        th.createDefaultExecutorsMap();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        super.tearDown();
    }

    /**
     * Test for adding and removing relation groups.
     * No relation pairs added; no relation pairs testing.
     * Loading relations with {@link BatchPresentation} test.
     */
    public void testAddRemoveRelationGroup() throws Exception {
        String groupName = "Relation1";
        String groupName2 = "Relation2";
        Relation relationGroup = relationService.createRelation(th.getAdminSubject(), groupName, groupName);
        Assert.assertEquals(groupName, relationGroup.getName());
        List<Relation> groups = relationService.getRelations(th.getAdminSubject(),
                AFProfileStrategy.RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(groups.get(0).getName(), groupName);
        Relation relationGroup2 = relationService.createRelation(th.getAdminSubject(), groupName2, groupName2);
        groups = relationService.getRelations(th.getAdminSubject(), AFProfileStrategy.RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation());
        Assert.assertEquals(2, groups.size());
        Assert.assertTrue((groups.get(0).getName().equals(groupName) && groups.get(1).getName().equals(groupName2))
                || (groups.get(0).getName().equals(groupName2) && groups.get(1).getName().equals(groupName)));
        relationService.removeRelation(th.getAdminSubject(), relationGroup.getId());
        groups = relationService.getRelations(th.getAdminSubject(), AFProfileStrategy.RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation());
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(groups.get(0).getName(), groupName2);
        relationService.removeRelation(th.getAdminSubject(), relationGroup2.getId());
        groups = relationService.getRelations(th.getAdminSubject(), AFProfileStrategy.RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation());
        Assert.assertEquals(0, groups.size());
    }

    /**
     * Add/remove relation pairs test.
     * Simple test for relation pair loading.
     */
    public void testAddRemoveRelation() throws Exception {
        String groupName = "Relation1";
        String groupName2 = "Relation2";
        Relation relationGroup = relationService.createRelation(th.getAdminSubject(), groupName, groupName);
        Relation relationGroup2 = relationService.createRelation(th.getAdminSubject(), groupName2, groupName2);
        Actor a1 = th.createActorIfNotExist("1", "1");
        Actor a2 = th.createActorIfNotExist("2", "2");
        Actor a3 = th.createActorIfNotExist("3", "3");
        relationService.addRelationPair(th.getAdminSubject(), relationGroup.getName(), a1, a3);
        relationService.addRelationPair(th.getAdminSubject(), relationGroup2.getName(), a2, a3);
        relationService.addRelationPair(th.getAdminSubject(), relationGroup.getName(), a1, a3);
        relationService.addRelationPair(th.getAdminSubject(), relationGroup2.getName(), a1, a3);
        List<RelationPair> relations = relationService.getRelationPairs(th.getAdminSubject(), groupName,
                AFProfileStrategy.RELATIONS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
        assertEquals(1, relations.size());
        assertEquals(a1, relations.get(0).getLeft());
        assertEquals(a3, relations.get(0).getRight());
        RelationPair toRemove = relations.get(0);
        relations = relationService.getRelationPairs(th.getAdminSubject(), groupName2,
                AFProfileStrategy.RELATIONS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
        assertEquals(2, relations.size());
        assertTrue(((relations.get(0).getLeft().equals(a2) && relations.get(0).getRight().equals(a3)) && (relations.get(1).getLeft().equals(a1) && relations
                .get(1).getRight().equals(a3)))
                || ((relations.get(1).getLeft().equals(a2) && relations.get(1).getRight().equals(a3)) && (relations.get(0).getLeft().equals(a1) && relations
                        .get(0).getRight().equals(a3))));
        relationService.removeRelationPair(th.getAdminSubject(), toRemove.getId());
        assertEquals(0, relationService.getRelationPairs(th.getAdminSubject(), groupName,
                AFProfileStrategy.RELATIONS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation()).size());
        relationService.removeRelation(th.getAdminSubject(), relationGroup.getId());
        relationService.removeRelation(th.getAdminSubject(), relationGroup2.getId());
    }
}
