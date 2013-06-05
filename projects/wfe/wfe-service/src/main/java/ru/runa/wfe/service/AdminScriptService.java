package ru.runa.wfe.service;

import javax.ejb.Remote;

import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.user.User;

/**
 * Service for remote script execution.
 * 
 * @author dofs
 * @since 4.0
 */
@Remote
public interface AdminScriptService {
    /**
     * Executes script.
     * 
     * @param user
     *            authorized user
     * @param configData
     *            script data
     * @param processDefinitionsBytes
     *            process definitions data to deploy or update
     * @throws AdminScriptException
     *             if script execution fails
     */
    public void run(User user, byte[] configData, byte[][] processDefinitionsBytes) throws AdminScriptException;
}
