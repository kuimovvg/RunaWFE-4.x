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
package ru.runa.service.af;

import java.util.List;

import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Created on 30.01.2006
 */
public interface SubstitutionService {

    public Substitution createSubstitution(User user, Substitution substitution) throws AuthorizationException;

    public List<Substitution> getSubstitutions(User user, Long actorId) throws AuthorizationException;

    public Substitution getSubstitution(User user, Long substitutionId) throws AuthorizationException;

    public void updateSubstitution(User user, Substitution substitution) throws AuthorizationException;

    public void deleteSubstitutions(User user, List<Long> substitutionIds) throws SubstitutionDoesNotExistException, AuthorizationException;

    public <T extends SubstitutionCriteria> void createCriteria(User user, T substitutionCriteria) throws AuthorizationException;

    public SubstitutionCriteria getCriteria(User user, Long substitutionCriteriaId) throws AuthorizationException;

    public List<SubstitutionCriteria> getAllCriterias(User user) throws AuthorizationException;

    public void updateCriteria(User user, SubstitutionCriteria criteria) throws AuthorizationException;

    public void deleteCriterias(User user, List<SubstitutionCriteria> criterias) throws AuthorizationException;

    public void deleteCriteria(User user, SubstitutionCriteria criteria) throws AuthorizationException;

    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria criteria) throws AuthorizationException;

}
