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

import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.User;

/**
 * Created on 30.01.2006
 * 
 */
public class SubstitutionServiceDelegate extends EJB3Delegate implements SubstitutionService {

    public SubstitutionServiceDelegate() {
        super(SubstitutionService.class);
    }

    private SubstitutionService getSubstitutionService() {
        return getService();
    }

    @Override
    public Substitution createSubstitution(User user, Substitution substitution) {
        return getSubstitutionService().createSubstitution(user, substitution);
    }

    @Override
    public List<Substitution> getSubstitutions(User user, Long actorId) {
        return getSubstitutionService().getSubstitutions(user, actorId);
    }

    @Override
    public void deleteSubstitutions(User user, List<Long> substitutionIds) {
        getSubstitutionService().deleteSubstitutions(user, substitutionIds);
    }

    @Override
    public Substitution getSubstitution(User user, Long substitutionId) {
        return getSubstitutionService().getSubstitution(user, substitutionId);
    }

    @Override
    public void updateSubstitution(User user, Substitution substitution) {
        getSubstitutionService().updateSubstitution(user, substitution);
    }

    @Override
    public <T extends SubstitutionCriteria> void createCriteria(User user, T substitutionCriteria) {
        getSubstitutionService().createCriteria(user, substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getCriteria(User user, Long criteriaId) {
        return getSubstitutionService().getCriteria(user, criteriaId);
    }

    @Override
    public SubstitutionCriteria getCriteriaByName(User user, String name) {
        return getSubstitutionService().getCriteriaByName(user, name);
    }

    @Override
    public List<SubstitutionCriteria> getAllCriterias(User user) {
        return getSubstitutionService().getAllCriterias(user);
    }

    @Override
    public void updateCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        getSubstitutionService().updateCriteria(user, substitutionCriteria);
    }

    @Override
    public void deleteCriterias(User user, List<SubstitutionCriteria> criterias) {
        getSubstitutionService().deleteCriterias(user, criterias);
    }

    @Override
    public void deleteCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        getSubstitutionService().deleteCriteria(user, substitutionCriteria);
    }

    @Override
    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        return getSubstitutionService().getSubstitutionsByCriteria(user, substitutionCriteria);
    }
}
