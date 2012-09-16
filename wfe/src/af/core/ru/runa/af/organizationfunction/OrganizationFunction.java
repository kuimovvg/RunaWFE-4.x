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
package ru.runa.af.organizationfunction;

import java.util.List;

/**
 * General abstraction for organization function Created on 08.02.2005
 * 
 */
public interface OrganizationFunction {

    /**
     * @param parameters - array of parameters
     * @return id's of executors
     * @throws OrganizationFunctionException
     */
    public List<Long> getExecutorIds(Object[] parameters) throws OrganizationFunctionException;
}
