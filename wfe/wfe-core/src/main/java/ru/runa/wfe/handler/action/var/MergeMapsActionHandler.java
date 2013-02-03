package ru.runa.wfe.handler.action.var;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

public class MergeMapsActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map<?, ?> map1 = handlerData.getInputParam(Map.class, "map1", null);
        if (map1 == null) {
            map1 = new HashMap();
        }
        Map<?, ?> map2 = handlerData.getInputParam(Map.class, "map1", null);
        if (map2 == null) {
            map2 = new HashMap();
        }
        Map resultMap = new HashMap(map1);
        resultMap.putAll(map2);
        handlerData.setOutputVariable("map", resultMap);
        log.debug("Merged to the map " + resultMap);
    }

}
