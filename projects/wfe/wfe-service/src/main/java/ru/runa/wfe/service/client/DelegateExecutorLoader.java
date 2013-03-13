package ru.runa.wfe.service.client;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.IExecutorLoader;
import ru.runa.wfe.user.User;

public class DelegateExecutorLoader implements IExecutorLoader {
    private final User user;

    public DelegateExecutorLoader(User user) {
        this.user = user;
    }

    @Override
    public Executor getExecutor(Long id) {
        return Delegates.getExecutorService().getExecutor(user, id);
    }

    @Override
    public Actor getActorByCode(Long code) {
        return Delegates.getExecutorService().getActorByCode(user, code);
    }

    @Override
    public Executor getExecutor(String name) {
        return Delegates.getExecutorService().getExecutorByName(user, name);
    }
}
