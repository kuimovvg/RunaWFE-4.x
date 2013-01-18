package ru.runa.common.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.Delegates;

public class InitializeDBListener implements ServletContextListener {
    public static Log log = LogFactory.getLog(InitializeDBListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("initializing database");
        Delegates.getInitializerService().init(false);
        log.info("initialization done");
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

}
