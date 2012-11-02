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
package ru.runa.wfe.commons.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;

/**
 * Safety guard for {@linkplain ChangeListener}.
 * Catches all exceptions during call and log it. 
 * @author Konstantinov Aleksey
 */
public class ChangeListenerGuard implements ChangeListener {

    /**
     * Logging support. 
     */
    private static final Log log = LogFactory.getLog(ChangeListenerGuard.class);

    /**
     * {@linkplain ChangeListener}, used to delegate calls. 
     */
    private final ChangeListener delegated;

    /**
     * Create guard for specified {@linkplain ChangeListener}. 
     * @param delegated {@linkplain ChangeListener}, which must be guarded.
     */
    public ChangeListenerGuard(ChangeListener delegated) {
        super();
        this.delegated = delegated;
    }

    @Override
    public void markTransactionComplete() {
        try {
            delegated.markTransactionComplete();
        } catch (Exception e) {
            log.error("markTransactionComplete() call failed on " + delegated.getClass().getName(), e);
        }
    }

    @Override
    public void onChange() {
        try {
            delegated.onChange();
        } catch (Exception e) {
            log.error("onChange() call failed on " + delegated.getClass().getName(), e);
        }
    }

    @Override
    public void onChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        try {
            delegated.onChange(object, currentState, previousState, propertyNames, types);
        } catch (Exception e) {
            log.error("onChange(object, currentState, previousState, propertyNames, types) call failed on " + delegated.getClass().getName(), e);
        }
    }

    @Override
    public void onTransactionComplete() {
        try {
            delegated.onTransactionComplete();
        } catch (Exception e) {
            log.error("onTransactionComplete() call failed on " + delegated.getClass().getName(), e);
        }
    }
}
