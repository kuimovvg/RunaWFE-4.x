package ru.runa.wfe.extension.handler.var;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class PutObjectToMapActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map map = handlerData.getInputParam(Map.class, "map", null);
        Object key = handlerData.getInputParam("key");
        Object object = handlerData.getInputParam("object", null);
        if (map == null) {
            map = new HashMap();
        }
        if (object != null) {
            map.put(key, object);
            if (handlerData.getOutputParams().containsKey("result")) {
                handlerData.setOutputParam("result", map);
            } else {
                // back compatibility
                handlerData.setOutputParam("map", map);
            }
        }
        log.debug("Object " + object + " set to the map " + map + " at " + key);
    }

}
