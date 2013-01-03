package ru.runa.service.wf;

import javax.ejb.Remote;
import javax.security.auth.Subject;

import ru.runa.wfe.script.AdminScriptException;

@Remote
public interface AdminScriptService {
    public void run(Subject subject, byte[] configData, byte[][] processDefinitionsBytes) throws AdminScriptException;
}
