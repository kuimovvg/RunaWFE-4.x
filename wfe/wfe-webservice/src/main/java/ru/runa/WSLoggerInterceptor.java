package ru.runa;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WSLoggerInterceptor {
    private static final Log log = LogFactory.getLog(WSLoggerInterceptor.class);

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } catch (Exception e) {
            log.error("webservice", e);
            throw e;
        }
    }

}
