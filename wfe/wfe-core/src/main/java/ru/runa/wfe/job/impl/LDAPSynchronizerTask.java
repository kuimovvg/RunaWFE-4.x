package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.security.logic.LDAPLogic;

public class LDAPSynchronizerTask extends JobTask {
    private LDAPLogic ldapLogic;

    @Required
    public void setLdapLogic(LDAPLogic ldapLogic) {
        this.ldapLogic = ldapLogic;
    }

    @Override
    protected void execute() throws Exception {
        if (SystemProperties.isLDAPSynchronizationEnabled()) {
            try {
                ldapLogic.synchronizeExecutors(false);
            } finally {
                CachingLogic.onTransactionComplete();
            }
        }
    }

}
