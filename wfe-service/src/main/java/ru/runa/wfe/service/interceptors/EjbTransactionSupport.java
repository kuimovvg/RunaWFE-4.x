package ru.runa.wfe.service.interceptors;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.LockAcquisitionException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.user.User;

import com.google.common.base.Throwables;

/**
 * It is important to understand that in a BMT, the message consumed by the MDB
 * is not part of the transaction. When an MDB uses container-managed
 * transactions, the message it handles is a part of the transaction, so if the
 * transaction is rolled back, the consumption of the message is also rolled
 * back, forcing the JMS provider to re-deliver the message. But with
 * bean-managed transactions, the message is not part of the transaction, so if
 * the BMT is rolled back, the JMS provider will not be aware of the
 * transaction’s failure. However, all is not lost, because the JMS provider can
 * still rely on message acknowledgment to determine whether the message was
 * delivered successfully.
 */
public class EjbTransactionSupport {
    private static final Log log = LogFactory.getLog(EjbTransactionSupport.class);
    @Resource
    private EJBContext ejbContext;

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        UserTransaction transaction = ejbContext.getUserTransaction();
        try {
            transaction.begin();
            if (ic.getParameters() != null && ic.getParameters().length > 0 && ic.getParameters()[0] instanceof User) {
                UserHolder.set((User) ic.getParameters()[0]);
            }
            long startTime = System.currentTimeMillis();
            Object result = invokeWithRetry(ic);
            long jobTime = System.currentTimeMillis() - startTime;
            if (jobTime > 2000) {
                log.info("Execution of " + DebugUtils.getDebugString(ic) + " took " + (jobTime / 1000) + " sec.");
            }
            startTime = System.currentTimeMillis();
            transaction.commit();
            jobTime = System.currentTimeMillis() - startTime;
            if (jobTime > 2000) {
                log.info("Commit of " + DebugUtils.getDebugString(ic) + " took " + (jobTime / 1000) + " sec.");
            }
            UserHolder.reset();
            return result;
        } catch (Throwable th) {
            rollbackTransaction(transaction);
            throw Throwables.propagate(th);
        } finally {
            CachingLogic.onTransactionComplete();
        }
    }

    private void rollbackTransaction(UserTransaction transaction) {
        int status = -1;
        try {
            if (transaction != null) {
                status = transaction.getStatus();
                if (status != Status.STATUS_NO_TRANSACTION) {
                    transaction.rollback();
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to rollback, status: " + status, e);
        }
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
            log.error("Got LockAcquisitionException: " + e);
            Thread.sleep(1000);
            return ic.proceed();
        }
    }

}
