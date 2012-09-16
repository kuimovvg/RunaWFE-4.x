package ru.runa.commons;

import ru.runa.wf.WFRunaHibernateInterceptor;

public class InfoHolder {
    public static final String UNASSIGNED_SWIMLANE_VALUE = ApplicationContextFactory.getDialectClassName().contains("Oracle") ? WFRunaHibernateInterceptor.ORACLE_EMPTY_TOKEN
            : "";

}
