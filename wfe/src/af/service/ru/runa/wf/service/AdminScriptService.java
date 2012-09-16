package ru.runa.wf.service;

import javax.ejb.Remote;

import ru.runa.af.AuthenticationException;
import ru.runa.wf.WfeScriptException;

@Remote
public interface AdminScriptService {
    public void run(String login, String password, byte[] configData, byte[][] processDefinitionsBytes) throws WfeScriptException,
            AuthenticationException;
}
