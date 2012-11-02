package ru.runa.service.interceptors;

import javax.ejb.EJBException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.wf.impl.MessagePostponedException;
import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Throwables;

/**
 * Interceptor for logging and original exception extractor (from {@link EJBException}).
 * 
 * @author Dofs
 * @since RunaWFE 4.0
 */
public class EjbExceptionSupport {
    private static final Log log = LogFactory.getLog(EjbExceptionSupport.class);

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } catch (Throwable th) {
        	if (th instanceof MessagePostponedException) {
        		log.info(th); // TODO debug
        		return null;
        	}
            log.error("ejb call", th);
            if (th instanceof EJBException) {
                Throwable cause = ((EJBException) th).getCause();
                Throwables.propagateIfInstanceOf(cause, Exception.class);
                throw new InternalApplicationException(cause);
            }
            Throwables.propagateIfInstanceOf(th, Exception.class);
            throw new InternalApplicationException(th);
        }
    }

}
