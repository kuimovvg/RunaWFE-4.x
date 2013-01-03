package ru.runa.wfe.handler.action.var;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

public class PutObjectToMapActionHandler extends ParamBasedActionHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction() throws Exception {
        Map map = getInputParam(Map.class, "map", null);
        Object key = getInputParam("key");
        Object object = getInputParam("object", null);
        if (map == null) {
            map = new HashMap();
        }
        if (object != null) {
            map.put(key, object);
            setOutputVariable("map", map);
        }
        log.debug("Object " + object + " set to the map " + map + " at " + key);
    }

}
