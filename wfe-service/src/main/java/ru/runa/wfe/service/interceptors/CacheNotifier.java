package ru.runa.wfe.service.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import ru.runa.wfe.commons.cache.CachingLogic;

public class CacheNotifier {

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        Object result = ic.proceed();
        CachingLogic.onTransactionComplete();
        return result;
    }

}
