package ru.runa.service.bot;

import javax.ejb.Remote;

@Remote
public interface BotInvokerService {

    public void invokeBots();

    public void startPeriodicBotsInvocation();

    public boolean isRunning();

    public void cancelPeriodicBotsInvocation();

}
