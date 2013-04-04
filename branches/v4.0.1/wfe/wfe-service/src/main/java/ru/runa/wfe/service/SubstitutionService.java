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
package ru.runa.wfe.service;

import java.util.List;

import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Created on 30.01.2006
 */
public interface SubstitutionService {

    public Substitution createSubstitution(User user, Substitution substitution);

    public List<Substitution> getSubstitutions(User user, Long actorId);

    public Substitution getSubstitution(User user, Long substitutionId);

    public void updateSubstitution(User user, Substitution substitution);

    public void deleteSubstitutions(User user, List<Long> substitutionIds) throws SubstitutionDoesNotExistException;

    public <T extends SubstitutionCriteria> void createCriteria(User user, T substitutionCriteria);

    public SubstitutionCriteria getCriteria(User user, Long criteriaId);

    /**
     * @return {@link SubstitutionCriteria} or <code>null</code>
     */
    public SubstitutionCriteria getCriteriaByName(User user, String name);

    public List<SubstitutionCriteria> getAllCriterias(User user);

    public void updateCriteria(User user, SubstitutionCriteria criteria);

    public void deleteCriterias(User user, List<SubstitutionCriteria> criterias);

    public void deleteCriteria(User user, SubstitutionCriteria criteria);

    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria criteria);

}
