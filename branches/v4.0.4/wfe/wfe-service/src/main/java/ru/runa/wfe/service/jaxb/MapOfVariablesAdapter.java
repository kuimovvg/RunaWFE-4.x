package ru.runa.wfe.service.jaxb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.Maps;

public class MapOfVariablesAdapter extends XmlAdapter<MapOfVariables, HashMap> {

    @Override
    public MapOfVariables marshal(HashMap map) {
        MapOfVariables variables = new MapOfVariables();
        for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) map.entrySet()) {
            variables.list.add(new Variable(entry.getKey(), entry.getValue()));
        }
        return variables;
    }

    @Override
    public HashMap unmarshal(MapOfVariables variables) {
        HashMap<String, Object> map = Maps.newHashMap();
        for (Variable variable : variables.list) {
            map.put(variable.name, variable.value);
        }
        return map;
    }

}
