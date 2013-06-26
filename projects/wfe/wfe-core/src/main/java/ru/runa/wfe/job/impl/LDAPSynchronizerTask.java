package ru.runa.wfe.job.impl;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.security.logic.LDAPLogic;

public class LDAPSynchronizerTask extends TimerTask {
    private static final Log log = LogFactory.getLog(LDAPSynchronizerTask.class);
    private LDAPLogic ldapLogic;

    @Required
    public void setLdapLogic(LDAPLogic ldapLogic) {
        this.ldapLogic = ldapLogic;
    }

    @Override
    public final void run() {
        if (SystemProperties.isLDAPSynchronizationEnabled()) {
            try {
                ldapLogic.synchronizeExecutors(false);
            } catch (Throwable th) {
                log.error("timer task error", th);
            } finally {
                CachingLogic.onTransactionComplete();
            }
        }
    }

}
