package ru.runa.wfe.service;

import javax.ejb.Remote;

@Remote
public interface InitializerService {

    public void init(boolean force);

}
