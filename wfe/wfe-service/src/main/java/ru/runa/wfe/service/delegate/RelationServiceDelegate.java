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
import ru.runa.wfe.relation.RelationDoesNotExistException;
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
    public RelationPair addRelationPair(User user, Long relationId, Executor from, Executor to) {
        return getRelationService().addRelationPair(user, relationId, from, to);
    }

    @Override
    public List<Relation> getRelations(User user, BatchPresentation batchPresentation) {
        return getRelationService().getRelations(user, batchPresentation);
    }

    @Override
    public Relation getRelationByName(User user, String name) {
        return getRelationService().getRelationByName(user, name);
    }

    @Override
    public Relation getRelation(User user, Long id) {
        return getRelationService().getRelation(user, id);
    }

    @Override
    public Relation createRelation(User user, Relation relation) {
        return getRelationService().createRelation(user, relation);
    }

    @Override
    public Relation updateRelation(User user, Relation relation) throws RelationDoesNotExistException {
        return getRelationService().updateRelation(user, relation);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsRight(User user, String name, List<? extends Executor> right) {
        return getRelationService().getExecutorsRelationPairsRight(user, name, right);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsLeft(User user, String name, List<? extends Executor> left) {
        return getRelationService().getExecutorsRelationPairsLeft(user, name, left);
    }

    @Override
    public List<RelationPair> getRelationPairs(User user, String name, BatchPresentation batchPresentation) {
        return getRelationService().getRelationPairs(user, name, batchPresentation);
    }

    @Override
    public void removeRelationPair(User user, Long id) {
        getRelationService().removeRelationPair(user, id);
    }

    @Override
    public void removeRelationPairs(User user, List<Long> ids) {
        getRelationService().removeRelationPairs(user, ids);
    }

    @Override
    public void removeRelation(User user, Long name) {
        getRelationService().removeRelation(user, name);
    }
}
