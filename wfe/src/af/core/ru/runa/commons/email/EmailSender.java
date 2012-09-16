package ru.runa.commons.email;

import java.util.Map;

import javax.security.auth.Subject;

public interface EmailSender {

    public void sendMessage(EmailResources resources, Subject subject, Map<String, Object> variables, String message) throws Exception;

    public void sendMessage(EmailResources resources, Subject subject, Map<String, Object> variables, Long tokenId, String tokenName)
            throws Exception;

}
