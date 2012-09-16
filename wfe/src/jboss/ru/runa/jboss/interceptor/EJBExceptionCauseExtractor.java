package ru.runa.jboss.interceptor;

import javax.ejb.EJBException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;

public class EJBExceptionCauseExtractor implements Interceptor {

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        try {
            return invocation.invokeNext();
        } catch (EJBException e) {
            throw e.getCause();
        }
    }
}
