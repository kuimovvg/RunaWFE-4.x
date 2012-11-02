package ru.runa.service.wf;

import javax.ejb.Remote;

import ru.runa.wfe.script.WfeScriptException;
import ru.runa.wfe.security.AuthenticationException;

@Remote
public interface AdminScriptService {
    public void run(String login, String password, byte[] configData, byte[][] processDefinitionsBytes) throws WfeScriptException,
            AuthenticationException;
}
