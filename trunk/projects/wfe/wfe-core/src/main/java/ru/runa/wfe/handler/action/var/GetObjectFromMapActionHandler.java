package ru.runa.wfe.handler.action.var;

import java.util.Map;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

import com.google.common.collect.Maps;

public class GetObjectFromMapActionHandler extends ParamBasedActionHandler {
    @Override
    protected void executeAction() throws Exception {
        Map<?, ?> map = getInputParam(Map.class, "map", null);
        if (map == null) {
            map = Maps.newHashMap();
        }
        Object object = map.get(getInputParam("key"));
        setOutputVariable("object", object);
    }

}
