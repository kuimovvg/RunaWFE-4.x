package ru.runa.wfe.job.impl;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.cache.CachingLogic;

/**
 * Base class for timer tasks with {@link CachingLogic} notification support.
 * 
 * @author dofs
 * @since 4.0
 */
public abstract class TransactionalTaskBase extends TimerTask {
    private static final Log log = LogFactory.getLog(TransactionalTaskBase.class);

    /**
     * @return should we reset cache and call doExecute()
     */
    protected boolean doPrerequsites() {
        return true;
    }

    /**
     * Execute work inside transaction (but create it yourself).
     */
    protected abstract void doExecute();

    @Override
    public final void run() {
        try {
            if (doPrerequsites()) {
                try {
                    CachingLogic.onTaskChange(null, null, null, null, null);
                    doExecute();
                } finally {
                    CachingLogic.onTransactionComplete();
                }
            }
        } catch (Throwable th) {
            log.error("timer task error", th);
        }
    }

}
