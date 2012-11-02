package ru.runa.service.wf;

import javax.ejb.Remote;

@Remote
public interface InitializerService {

    public void init(boolean force);

}
