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
package ru.runa.af.dao;

import java.util.List;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;

/**
 * DAO level interface for managing {@linkplain Substitution}'s.
 * Created on 27.01.2006
 * @author Semochkin_v
 * @author Gordienko_m
 */
public interface SubstitutionDAO {

    /**
     * Load all {@linkplain Substitution}'s.
     * @return Array of {@linkplain Substitution}'s.
     */
    public List<Substitution> getAllSubstitutions();

    /**
     * Load {@linkplain Substitution} by identity. Throws {@linkplain InternalApplicationException} if no substitution found.
     * @param id {@linkplain Substitution} identity to load.
     * @return Loaded {@linkplain Substitution}.
     */
    public Substitution getSubstitution(Long id);

    /**
     * Load {@linkplain Substitution}'s by identity. Throws {@linkplain InternalApplicationException} if at least one substitution not found.
     * Result {@linkplain Substitution}'s order is not specified.
     * @param ids {@linkplain Substitution}'s identity to load.
     * @return Loaded {@linkplain Substitution}'s.
     */
    public List<Substitution> getSubstitutions(List<Long> ids);

    /**
     * Save or update {@linkplain Substitution}.
     * @param substitution {@linkplain Substitution} to save/update.
     */
    public void storeSubstitution(Substitution substitution);

    /**
     * Remove {@linkplain Substitution}'s.
     * @param substitutionIds Removed {@linkplain Substitution}'s identity.
     */
    public void deleteSubstitution(Long id);

    /**
     * Remove {@linkplain Substitution}'s.
     * @param ids Removed {@linkplain Substitution}'s identity.
     */
    public void deleteSubstitutions(List<Long> ids);

    /**
     * Loads all {@linkplain Substitution}'s for {@linkplain Actor}.
     * Loaded {@linkplain Substitution}'s is ordered by substitution position.
     * @param actorId {@linkplain Actor} identity to load {@linkplain Substitution}'s.
     * @return {@linkplain Substitution}'s for {@linkplain Actor}.
     */
    public List<Substitution> getActorSubstitutions(Long actorId);

    public void createCriteria(SubstitutionCriteria criteria);

    public SubstitutionCriteria getCriteria(Long id);

    public List<SubstitutionCriteria> getAllCriterias();

    public void storeCriteria(SubstitutionCriteria criteria);

    public void storeCriterias(List<SubstitutionCriteria> criterias);

    public void deleteCriteria(Long id);

    public void deleteCriteria(SubstitutionCriteria criteria);

    public List<Substitution> getSubstitutionsByCriteria(SubstitutionCriteria criteria);
}
