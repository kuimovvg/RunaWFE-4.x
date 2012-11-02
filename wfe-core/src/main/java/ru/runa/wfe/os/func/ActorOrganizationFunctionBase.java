package ru.runa.wfe.os.func;

import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.os.OrgFunctionBase;

import com.google.common.base.Preconditions;

public abstract class ActorOrganizationFunctionBase extends OrgFunctionBase {

    @Override
    protected List<Long> getExecutorCodes(Object... parameters) {
        Preconditions.checkNotNull(parameters, "parameters");
        Preconditions.checkArgument(parameters.length == 1, "expected parameters with 1 element");
        Long actorCode = TypeConversionUtil.convertTo(parameters[0], Long.class);
        return getExecutorCodes(actorCode);
    }

    protected abstract List<Long> getExecutorCodes(Long actorCode);

}
