package ru.runa.service.interceptors;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.security.auth.Subject;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.hibernate.exception.LockAcquisitionException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.security.auth.SubjectHolder;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;

public class EjbTransactionSupport {
    @Resource
    private EJBContext ejbContext;

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        UserTransaction transaction = ejbContext.getUserTransaction();
        try {
            transaction.begin();
            System.out.println("=== " + ic.getMethod().getDeclaringClass().getName() + "." + ic.getMethod().getName() + "("
                    + Joiner.on(", ").join(getDebugArguments(ic.getParameters())) + ")");
            if (ic.getParameters().length > 0 && ic.getParameters()[0] instanceof Subject) {
                SubjectHolder.set((Subject) ic.getParameters()[0]);
            }
            Object result = invokeWithRetry(ic);
            transaction.commit();
            SubjectHolder.reset();
            return result;
        } catch (Throwable th) {
            rollbackTransaction(transaction);
            Throwables.propagateIfInstanceOf(th, Exception.class);
            throw new InternalApplicationException(th);
        } finally {
            CachingLogic.onTransactionComplete();
        }
    }

    private void rollbackTransaction(UserTransaction transaction) {
        int status = -1;
        try {
            status = transaction.getStatus();
            if (status != Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to rollback, status: " + status, e);
        }
    }

    private String[] getDebugArguments(Object[] values) {
        String[] strings = new String[values.length];
        for (int i = 0; i < strings.length; i++) {
            Object object = values[i];
            String string;
            if (object == null) {
                string = "null";
            } else if (object instanceof Subject) {
                try {
                    string = SubjectPrincipalsHelper.getActor((Subject) object).getName();
                } catch (Exception e) {
                    string = null;
                }
            } else {
                string = object.toString();
            }
            strings[i] = string;
        }
        return strings;
    }

    /**
     * Make invocation with retry on deadlock.
     * 
     * @param invocation
     *            Current invocation.
     * @return Invocation result.
     */
    private Object invokeWithRetry(InvocationContext ic) throws Throwable {
        try {
            return ic.proceed();
        } catch (LockAcquisitionException e) {
            Thread.sleep(1000);
            return ic.proceed();
        }
    }

}
