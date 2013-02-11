package ru.runa.service.jaxb;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Maps;

public class MapOfVariablesAdapter extends XmlAdapter<MapOfVariables, Map> {

    @Override
    public MapOfVariables marshal(Map map) {
        MapOfVariables variables = new MapOfVariables();
        for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) map.entrySet()) {
            variables.list.add(new Variable(entry.getKey(), entry.getValue()));
        }
        return variables;
    }

    @Override
    public Map unmarshal(MapOfVariables variables) {
        Map<String, Object> map = Maps.newHashMap();
        for (Variable variable : variables.list) {
            map.put(variable.name, variable.value);
        }
        return map;
    }

}
