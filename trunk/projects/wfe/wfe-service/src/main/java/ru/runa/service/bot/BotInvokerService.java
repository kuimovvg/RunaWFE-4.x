package ru.runa.service.bot;

import javax.ejb.Remote;

import ru.runa.wfe.bot.BotInvokerException;

@Remote
public interface BotInvokerService {
    
    public void invokeBots() throws BotInvokerException;

    public void startPeriodicBotsInvocation() throws BotInvokerException;
    
    public boolean isRunning();
    
    public void cancelPeriodicBotsInvocation();
    
}
