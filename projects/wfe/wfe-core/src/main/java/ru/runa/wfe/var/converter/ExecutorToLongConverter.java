package ru.runa.wfe.var.converter;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.Converter;

public class ExecutorToLongConverter implements Converter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean supports(Object value) {
        if (value == null) {
            return true;
        }
        return value instanceof Executor;
    }

    @Override
    public Object convert(Object o) {
        return ((Executor) o).getId();
    }

    @Override
    public Object revert(Object o) {
        Long executorId = (Long) o;
        if (executorId == null) {
            return null;
        }
        return ApplicationContextFactory.getExecutorDAO().getExecutor(executorId);
    }
}
