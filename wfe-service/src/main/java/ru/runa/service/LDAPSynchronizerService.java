package ru.runa.service;

import javax.ejb.Remote;

@Remote
public interface LDAPSynchronizerService {

    public void importExecutorsFromLDAP(String username, String password);
    
}
