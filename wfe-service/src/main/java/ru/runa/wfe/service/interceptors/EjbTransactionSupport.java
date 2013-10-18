package ru.runa.wfe.service.interceptors;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
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
    @Resource
    private EJBContext ejbContext;

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        UserTransaction transaction = ejbContext.getUserTransaction();
        try {
            transaction.begin();
            if (ic.getParameters() != null && ic.getParameters().length > 0 && ic.getParameters()[0] instanceof User) {
                User user = (User) ic.getParameters()[0];
                SubjectPrincipalsHelper.validateUser(user);
                UserHolder.set(user);
            }
            Object result = invokeWithRetry(ic);
            transaction.commit();
            UserHolder.reset();
            return result;
        } catch (Throwable th) {
            Utils.rollbackTransaction(transaction);
            throw Throwables.propagate(th);
        } finally {
            CachingLogic.onTransactionComplete();
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
        } catch (ConcurrencyFailureException e) {
            LogFactory.getLog(getClass()).error("Got ConcurrencyFailureException: " + e);
            Thread.sleep(1000);
            return ic.proceed();
        }
    }

}
