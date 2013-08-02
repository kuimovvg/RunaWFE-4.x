package ru.runa.wfe.service.interceptors;

import java.util.List;

import javax.ejb.EJBException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.impl.MessagePostponedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.validation.ValidationException;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Interceptor for logging and original exception extractor (from
 * {@link EJBException}).
 * 
 * @author Dofs
 * @since RunaWFE 4.0
 */
public class EjbExceptionSupport {
    private static final Log log = LogFactory.getLog(EjbExceptionSupport.class);

    private static final List<Class<? extends Exception>> warnExceptionClasses = Lists.newArrayList();
    static {
        warnExceptionClasses.add(AuthenticationException.class);
        warnExceptionClasses.add(AuthorizationException.class);
        warnExceptionClasses.add(ValidationException.class);
        warnExceptionClasses.add(TaskDoesNotExistException.class);
    }

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } catch (Throwable th) {
            if (th instanceof MessagePostponedException) {
                log.debug(th);
                throw (MessagePostponedException) th;
            }
            if (warnExceptionClasses.contains(th.getClass())) {
                log.warn("ejb call " + th);
            } else {
                log.error("ejb call error: " + DebugUtils.getDebugString(ic, true), th);
            }
            Throwables.propagateIfInstanceOf(th, Exception.class);
            throw Throwables.propagate(th);
        }
    }

}
