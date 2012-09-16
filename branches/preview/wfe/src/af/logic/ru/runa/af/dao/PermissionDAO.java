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

import java.util.Collection;
import java.util.List;

import ru.runa.af.Executor;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.UnapplicablePermissionException;

/**
  * DAO level interface for managing {@linkplain Permission}. 
  * @author Konstantinov Aleksey 19.02.2012
  */
public interface PermissionDAO {

    /**
     * Returns an array of Permission that executor has on securedObject. Returns as own permissions on securedObject as inherited group(s)
     * permissions on securedObject.
     * @param executor Executor for loading permissions.
     * @param securedObject SecuredObject for loading permissions.
     * @return Array of {@linkplain Permission} on secured object for Executor.
     */
    public List<Permission> getPermissions(Executor executor, SecuredObject securedObject);

    /**
     * Returns an array of Permission that executor itself has on securedObject. Inherited permissions are not returned.
     * @param executor Executor for loading permissions.
     * @param securedObject SecuredObject for loading permissions.
     * @return Array of {@linkplain Permission} on secured object for Executor.
     */
    public List<Permission> getOwnPermissions(Executor executor, SecuredObject securedObject);

    /**
     * Sets permissions for executor on securedObject.
     * @param executor Executor, which got permissions.
     * @param permissions Permissions for executor.
     * @param securedObject Secured object to set permission on.
     */
    public void setPermissions(Executor executor, Collection<Permission> permissions, SecuredObject securedObject) throws UnapplicablePermissionException;

    /**
     * Sets permissions for executors on securedObject.
     * @param executor Executors, which got permissions.
     * @param permissions Permissions for executor.
     * @param securedObject Secured object to set permission on.
     */
    public void setPermissions(Collection<? extends Executor> executors, Collection<Permission> permissions, SecuredObject securedObject) throws UnapplicablePermissionException;

    /**
     * Sets permissions for executors on securedObject.
     * @param executor Executors, which got permissions.
     * @param permissions Permissions for executors.
     * @param securedObject Secured object to set permission on.
     */
    public void setPermissions(List<Executor> executors, List<Collection<Permission>> permissions, SecuredObject securedObject) throws UnapplicablePermissionException;

    /**
     * Checks whether executor has permission on securedObject.
     * @param executor Executor, which permission must be check.
     * @param permission Checking permission.
     * @param securedObject Secured object to check permission on.
     * @return true if executor has requested permission on secuedObject; false otherwise.
     */
    public boolean isAllowed(Executor executor, Permission permission, SecuredObject securedObject);

    /**
     * Checks whether executor has permission on securedObject's. Create result array in same order, as securedObject's.
     * @param executor Executor, which permission must be check.
     * @param permission Checking permission.
     * @param securedObject Secured objects to check permission on.
     * @return Array of: true if executor has requested permission on secuedObject; false otherwise.
     */
    public boolean[] isAllowed(Executor executor, Permission permission, List<SecuredObject> securedObjects);
    
    public void deleteAllPermissions(Executor executor);
    
}
