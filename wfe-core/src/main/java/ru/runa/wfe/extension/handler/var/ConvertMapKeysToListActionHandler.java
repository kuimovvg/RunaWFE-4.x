package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

import com.google.common.collect.Maps;

public class ConvertMapKeysToListActionHandler extends CommonParamBasedHandler {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map<?, ?> map = handlerData.getInputParam(Map.class, "map", null);
        if (map == null) {
            map = Maps.newHashMap();
        }
        List list = new ArrayList(map.keySet());
        if (list.size() > 0 && list.get(0) instanceof Comparable) {
            Collections.sort(list);
        }
        handlerData.setOutputVariable("list", list);
    }

}
