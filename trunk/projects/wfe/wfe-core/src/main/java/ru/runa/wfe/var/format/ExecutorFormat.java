package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class ExecutorFormat implements VariableFormat<Executor>, VariableDisplaySupport<Executor> {

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
        return object.getFullName();
    }

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, Executor value) {
        return FormatCommons.getVarOut(user, value, webHelper, processId, name, 0, null);
    }

}
