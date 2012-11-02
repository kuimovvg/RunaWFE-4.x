package ru.runa.service.af;

import javax.ejb.Remote;

@Remote
public interface LDAPSynchronizerService {

    public void importExecutorsFromLDAP(String username, String password);
    
}
