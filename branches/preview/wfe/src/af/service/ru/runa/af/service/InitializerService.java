package ru.runa.af.service;

import javax.ejb.Remote;

@Remote
public interface InitializerService {

    public void init(boolean force, boolean isArchiveDBinit);
    
}
