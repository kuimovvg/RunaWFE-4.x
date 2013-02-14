package ru.runa.wfe.var.dto;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WfVariables {

    public static List<WfVariable> toList(Map<String, Object> map) {
        List<WfVariable> variables = Lists.newArrayList();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                variables.add(new WfVariable(entry.getKey(), entry.getValue()));
            }
        }
        return variables;
    }

    public static Map<String, Object> toMap(List<WfVariable> variables) {
        Map<String, Object> map = Maps.newHashMap();
        if (variables != null) {
            for (WfVariable variable : variables) {
                map.put(variable.getName(), variable.getValue());
            }
        }
        return map;
    }

    // TODO post proc public static List<WfVariable> p
}
