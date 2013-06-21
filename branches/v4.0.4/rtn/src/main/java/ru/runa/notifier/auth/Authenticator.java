package ru.runa.notifier.auth;

import ru.runa.wfe.user.User;

public interface Authenticator {

    User authenticate() throws Exception;

    String getParamForWeb();

    boolean isRetryDialogEnabled();
}
