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
package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Local implementation for {@link RelationServiceDelegate}.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 */
public class RelationServiceDelegate extends EJB3Delegate implements RelationService {

    public RelationServiceDelegate() {
        super(RelationService.class);
    }

    private RelationService getRelationService() {
        return getService();
    }

    @Override
    public RelationPair addRelationPair(User user, String relationGroupName, Executor from, Executor to) {
        return getRelationService().addRelationPair(user, relationGroupName, from, to);
    }

    @Override
    public List<Relation> getRelations(User user, BatchPresentation batchPresentation) {
        return getRelationService().getRelations(user, batchPresentation);
    }

    @Override
    public Relation getRelationByName(User user, String relationGroupName) {
        return getRelationService().getRelationByName(user, relationGroupName);
    }

    @Override
    public Relation getRelation(User user, Long relationId) {
        return getRelationService().getRelation(user, relationId);
    }

    @Override
    public Relation createRelation(User user, String name, String description) {
        return getRelationService().createRelation(user, name, description);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsRight(User user, String relationName, List<? extends Executor> right) {
        return getRelationService().getExecutorsRelationPairsRight(user, relationName, right);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsLeft(User user, String relationName, List<? extends Executor> left) {
        return getRelationService().getExecutorsRelationPairsLeft(user, relationName, left);
    }

    @Override
    public List<RelationPair> getRelationPairs(User user, String relationName, BatchPresentation batchPresentation) {
        return getRelationService().getRelationPairs(user, relationName, batchPresentation);
    }

    @Override
    public void removeRelationPair(User user, Long relationId) {
        getRelationService().removeRelationPair(user, relationId);
    }

    @Override
    public void removeRelation(User user, Long relationGroupName) {
        getRelationService().removeRelation(user, relationGroupName);
    }
}
