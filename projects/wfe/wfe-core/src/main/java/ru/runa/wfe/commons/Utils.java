package ru.runa.wfe.commons;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;

public class Utils {

    public static void rollbackTransaction(UserTransaction transaction) {
        int status = -1;
        try {
            if (transaction != null) {
                status = transaction.getStatus();
                if (status != Status.STATUS_NO_TRANSACTION && status != Status.STATUS_ROLLEDBACK) {
                    transaction.rollback();
                } else {
                    LogFactory.getLog(Utils.class).warn("Unable to rollback, status: " + status);
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to rollback, status: " + status, e);
        }
    }

}
