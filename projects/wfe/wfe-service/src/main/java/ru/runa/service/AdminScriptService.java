package ru.runa.service;

import javax.ejb.Remote;

import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.user.User;

@Remote
public interface AdminScriptService {
    public void run(User user, byte[] configData, byte[][] processDefinitionsBytes) throws AdminScriptException;
}
