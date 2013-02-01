package ru.runa.wfe.os.func;

import java.util.List;

import ru.runa.wfe.os.OrgFunction;
import ru.runa.wfe.os.OrgFunctionException;
import ru.runa.wfe.user.Executor;

public class NullOrgFunction extends OrgFunction {

    @Override
    public List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException {
        return null;
    }

}
