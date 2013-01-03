package ru.runa.wfe.handler.action.var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

import com.google.common.collect.Maps;

public class ConvertMapValuesToListActionHandler extends ParamBasedActionHandler {

    @Override
    protected void executeAction() throws Exception {
        Map<?, ?> map = getInputParam(Map.class, "map", null);
        if (map == null) {
            map = Maps.newHashMap();
        }
        List list = new ArrayList(map.values());
        if (list.size() > 0 && list.get(0) instanceof Comparable) {
            Collections.sort(list);
        }
        setOutputVariable("list", list);
    }
}
