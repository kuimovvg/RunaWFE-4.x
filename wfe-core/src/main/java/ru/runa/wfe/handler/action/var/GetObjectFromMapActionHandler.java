package ru.runa.wfe.handler.action.var;

import java.util.Map;

import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

import com.google.common.collect.Maps;

public class GetObjectFromMapActionHandler extends CommonParamBasedHandler {
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map<?, ?> map = handlerData.getInputParam(Map.class, "map", null);
        if (map == null) {
            map = Maps.newHashMap();
        }
        Object object = map.get(handlerData.getInputParam("key"));
        handlerData.setOutputVariable("object", object);
    }

}
