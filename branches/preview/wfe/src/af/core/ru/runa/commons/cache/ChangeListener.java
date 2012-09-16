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
package ru.runa.commons.cache;

import org.hibernate.type.Type;

/**
 * Interface for components, receiving events on objects change and transaction complete.
 * All methods from interface is called under {@link CachingLogic} class synchronization.
 * Components, receiving events must implement one or more sub interface of {@link ChangeListener} and
 * register self in {@link CachingLogic}.
 */
public interface ChangeListener {
    /**
     * Called, then unrecognized object changed.
     */
    public void onChange();

    /**
     * Called, then changed one of predefined object (e. q. specific sub interface exists).  
     * @param object Changed object.
     * @param currentState Current state of object properties.
     * @param previousState Previous state of object properties.
     * @param propertyNames Property names (same order as in currentState).
     * @param types Property types.
     */
    public void onChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types);

    /**
     * Called, then transaction in current thread is completed.
     * Cache controller must mark transaction as completed, but must not recreate cache.<p/>
     * Cache recreation may be done in {@link #onTransactionComplete()}, then all caches is marked transaction.<p/>
     * {@link CachingLogic} guarantees, what all caches receive {@link #markTransactionComplete()}, and only after 
     * what all caches receive {@link #onTransactionComplete()}. 
     */
    public void markTransactionComplete();

    /**
     * Called, then transaction in current thread is completed.
     * Cache controller may recreate cache.<p/>
     * Completed transaction is already marked in {@link #markTransactionComplete()} call.<p/>
     * {@link CachingLogic} guarantees, what all caches receive {@link #markTransactionComplete()}, and only after 
     * what all caches receive {@link #onTransactionComplete()}. 
     */
    public void onTransactionComplete();
}
