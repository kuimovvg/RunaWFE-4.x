package ru.runa.wfe.handler.action.var;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

public class MergeMapsActionHandler extends ParamBasedActionHandler {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void executeAction() throws Exception {
        Map<?, ?> map1 = getInputParam(Map.class, "map1", null);
        if (map1 == null) {
            map1 = new HashMap();
        }
        Map<?, ?> map2 = getInputParam(Map.class, "map1", null);
        if (map2 == null) {
            map2 = new HashMap();
        }
        Map resultMap = new HashMap(map1);
        resultMap.putAll(map2);
        setOutputVariable("map", resultMap);
        log.debug("Merged to the map " + resultMap);
    }

}
