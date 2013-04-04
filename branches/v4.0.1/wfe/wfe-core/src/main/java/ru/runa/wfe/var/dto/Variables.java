package ru.runa.wfe.var.dto;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class Variables {

    public static Map<String, Object> toMap(List<WfVariable> variables) {
        Map<String, Object> map = Maps.newHashMap();
        for (WfVariable variable : variables) {
            map.put(variable.getDefinition().getName(), variable.getValue());
        }
        return map;
    }
}
