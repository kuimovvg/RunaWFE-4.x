package ru.runa.common.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.delegate.impl.InitializerServiceDelegateRemoteImpl;

public class InitializeDBListener implements ServletContextListener {
    public static Log log = LogFactory.getLog(InitializeDBListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            log.info("initializing database");
            initDatabase();
            log.info("initialization done");
        } catch (RuntimeException e) {
            log.fatal("initialization failed", e);
        }
    }

    private void initDatabase() {
        new InitializerServiceDelegateRemoteImpl().init(false, false);
    }

    private void initArchiveDatabase() {
        new InitializerServiceDelegateRemoteImpl().init(false, true);
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

}
