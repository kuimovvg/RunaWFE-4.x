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
package ru.runa.jboss.interceptor;

import javax.ejb.EJBException;
import javax.security.auth.Subject;

import org.hibernate.exception.LockAcquisitionException;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

import ru.runa.InternalApplicationException;
import ru.runa.af.authenticaion.SubjectHolder;
import ru.runa.commons.cache.CachingLogic;

/**
  * RunaWFE intercepter for jboss.
  * Do follows:<br/>
  * <li>Save current actor {@linkplain Subject} into {@linkplain SubjectHolder} to use inside action handlers and so on.
  * <li>Notify {@linkplain CachingLogic} on transaction complete.
  * <li>Retry call if deadlock or so on occurred. 
  * @author Konstantinov Aleksey 02.03.2012
  */
public class CachingLogicInterceptor implements Interceptor {
    private static ThreadLocal<Long> callCount = new ThreadLocal<Long>();

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        Long l = callCount.get();
        if (l != null && l.longValue() > 0) {
            System.out.println("========================================================= invocation: " + invocation + ": " + l);
        }
        if (invocation instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation) invocation;
            try {
                Long current = (callCount.get() == null ? new Long(1) : callCount.get() + 1);
                callCount.set(current);
                Object[] args = methodInvocation.getArguments();
                if (args.length > 0 && args[0] instanceof Subject && (current == 1 || SubjectHolder.get() == null)) {
                    SubjectHolder.set((Subject) args[0]);
                }
                return invokeWithRetry(invocation);
            } finally {
                Long current = callCount.get() - 1;
                callCount.set(current);
                if (current == 0) {
                    CachingLogic.onTransactionComplete();
                    SubjectHolder.reset();
                }
            }
        }
        return invocation.invokeNext();
    }

    /**
     * Make invocation with retry on deadlock.  
     * @param invocation Current invocation.
     * @return Invocation result.
     */
    private Object invokeWithRetry(Invocation invocation) throws Throwable {
        try {
            return invocation.invokeNext();
        } catch (RuntimeException e) {
            if (needRetry(e)) {
                Thread.sleep(1000);
                return invocation.invokeNext();
            }
            throw e;
        }
    }

    /**
     * Check's if call must be do again. Call is repeated, if deadlock or so on occurred. 
     * @param exception Exception, occurred during call.
     * @return Returns true, if call must be repeated and false to rethrow exception.
     */
    private boolean needRetry(Throwable exception) {
        if (exception instanceof EJBException || exception instanceof InternalApplicationException) {
            return needRetry(exception.getCause());
        }
        return exception != null && exception instanceof LockAcquisitionException;
    }
}
