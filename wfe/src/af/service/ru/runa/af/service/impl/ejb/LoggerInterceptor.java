package ru.runa.af.service.impl.ejb;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerInterceptor {
    private static final Log log = LogFactory.getLog(LoggerInterceptor.class);

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } catch (Exception e) {
            log.error("ejb", e);
            throw e;
        }
    }
    
}
