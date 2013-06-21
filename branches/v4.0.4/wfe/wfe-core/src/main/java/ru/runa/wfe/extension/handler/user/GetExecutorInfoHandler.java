package ru.runa.wfe.extension.handler.user;

import org.apache.commons.beanutils.BeanUtils;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Executor;

public class GetExecutorInfoHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Executor executor = handlerData.getInputParam(Executor.class, "executor");
        String format = handlerData.getInputParam("format");
        String result = BeanUtils.getProperty(executor, format);
        handlerData.setOutputParam("result", result);
    }

}
