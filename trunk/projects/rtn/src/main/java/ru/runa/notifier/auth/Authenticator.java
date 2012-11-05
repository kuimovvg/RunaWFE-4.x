package ru.runa.notifier.auth;

import javax.security.auth.Subject;

public interface Authenticator {

    Subject authenticate() throws Exception;
    String getParamForWeb();
    boolean isRetryDialogEnabled();
}
