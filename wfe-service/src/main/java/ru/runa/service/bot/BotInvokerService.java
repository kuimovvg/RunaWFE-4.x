package ru.runa.service.bot;

import javax.ejb.Remote;

import ru.runa.wfe.bot.BotStation;

@Remote
public interface BotInvokerService {

    public void invokeBots(BotStation botStation);

    public void startPeriodicBotsInvocation(BotStation botStation);

    public boolean isRunning();

    public void cancelPeriodicBotsInvocation();

}
