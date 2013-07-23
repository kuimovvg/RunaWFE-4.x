package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Executor;

public class ExecutorFormat implements VariableFormat<Executor> {

    @Override
    public Class<? extends Executor> getJavaClass() {
        return Executor.class;
    }

    @Override
    public Executor parse(String[] source) throws Exception {
        return TypeConversionUtil.convertTo(Executor.class, source[0]);
    }

    @Override
    public String format(Executor object) {
        return object.getLabel();
    }

}
